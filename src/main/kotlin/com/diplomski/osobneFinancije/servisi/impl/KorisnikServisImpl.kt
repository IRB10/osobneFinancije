package com.diplomski.osobneFinancije.servisi.impl

import com.diplomski.osobneFinancije.entiteti.*
import com.diplomski.osobneFinancije.forme.FinancesForm
import com.diplomski.osobneFinancije.forme.ProfileForm
import com.diplomski.osobneFinancije.forme.RacunForma
import com.diplomski.osobneFinancije.forme.RegisterForm
import com.diplomski.osobneFinancije.repozitoriji.*
import com.diplomski.osobneFinancije.servisi.KorisnikServis
import com.diplomski.osobneFinancije.servisi.NotificationService
import com.diplomski.osobneFinancije.utils.GeneratorPdfIzvjesca
import com.diplomski.osobneFinancije.utils.Konstante.EntryDetails.Companion.entryDetailsIncome
import com.diplomski.osobneFinancije.utils.Konstante.TimeManagament.Companion.ONE_MINUTE_IN_MILLIS
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.MessageSource
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.util.ObjectUtils
import java.io.ByteArrayInputStream
import java.time.LocalDateTime
import java.util.*
import java.util.stream.Collectors


@Service
@Transactional
class KorisnikServisImpl(
    private val korisnikRepozitorij: KorisnikRepozitorij,
    private val transakcijaRepozitorij: TransakcijaRepozitorij,
    private val verifikacijskiTokenRepozitorij: VerifikacijskiTokenRepozitorij,
    private val tokenRepozitorij: TokenRepozitorij,
    private val ulogaRepozitorij: UlogaRepozitorij,
    private val racunRepozitorij: RacunRepozitorij,
    private val prijenosRepozitorij: PrijenosRepozitorij,
    private val notificationService: NotificationService,
    @param:Qualifier("messageSource") private val poruke: MessageSource
) : KorisnikServis {

    override fun azurirajTransakciju(transakcija: Transakcija) {
        if (!ObjectUtils.isEmpty(transakcijaRepozitorij.findByNaziv(transakcija.naziv!!))) {
            val transakcija1 = transakcijaRepozitorij.findByNaziv(transakcija.naziv!!)
            transakcijaRepozitorij.save(preslikajTransakciju(transakcija1, transakcija))
        } else {
            val transakcija1 = Transakcija()
            transakcijaRepozitorij.save(preslikajTransakciju(transakcija1, transakcija))
        }
    }

    override fun dohvatiTransakcijeZaKorisnika(username: String): List<Transakcija> {
        return transakcijaRepozitorij.findAll().stream()
            .filter { entry ->
                !ObjectUtils.isEmpty(entry.transakcijaOd) && entry.transakcijaOd.equals(
                    username
                ) || entry.transakcijaPrema.equals(
                    username
                )
            }
            .collect(Collectors.toList<Transakcija>())
    }

    override fun spremiTransakciju(transakcija: Transakcija) {
        transakcijaRepozitorij.save(transakcija)
    }

    override fun dohvatiSveTransakcije(): List<Transakcija> {
        return transakcijaRepozitorij.findAll()
    }

    override fun dohvatiSveRacune(): List<Racun> {
        return racunRepozitorij.findAll()
    }

    override fun dohvatiOstaleKorisnike(): List<Korisnik?> {
        return korisnikRepozitorij.findAll()
            .minus(korisnikRepozitorij.findByKorisnickoIme(SecurityContextHolder.getContext().authentication.name))
    }

    @Transactional
    override fun kreirajRacun(racunForma: RacunForma) {
        val korisnik = korisnikRepozitorij.findByKorisnickoIme(SecurityContextHolder.getContext().authentication.name)
        val racun = Racun()
        racun.iznos = 0.0
        racun.aktivan = true
        racun.opis = racunForma.opisRacuna
        racun.vrstaRacuna = racunForma.vrstaRacuna.toString()
        racun.korisnici = Collections.singleton(korisnik)
        korisnik!!.racuni.add(racun)
        korisnikRepozitorij.save(korisnik)
        racunRepozitorij.save(racun)
    }

    @Transactional
    override fun deaktivirajRacun(racun: Racun) {
        racun.aktivan = false
        racunRepozitorij.save(racun)
    }

    @Transactional
    override fun obrisiRacun(racun: Racun) {
        racunRepozitorij.delete(racun)
    }

    override fun dohvatiRacuneKorisnika(): List<Racun> {
        val korisnik = korisnikRepozitorij.findByKorisnickoIme(SecurityContextHolder.getContext().authentication.name)
        if (korisnik != null) {
            return korisnik.racuni.toList()
        }
        return emptyList()
    }

    @Transactional
    override fun updateObligation(
        financesForm: FinancesForm,
        korisnickoIme: String,
        obligationType: String,
        kategorija: Kategorija?,
        korisnikId: Long?,
        racunId: Long?,
        locale: Locale
    ) {
        val transakcija = Transakcija("Obligation - " + obligationType + " : " + financesForm.obligationDate)
        transakcija.datumKreiranja = LocalDateTime.now()
        transakcija.vrijednost = financesForm.value.toDouble()
        transakcija.opis = financesForm.obligationDetails
        transakcija.danPlacanja = financesForm.obligationDate
        transakcija.transakcijaOd = korisnickoIme
        transakcija.korisnik = korisnikRepozitorij.findByKorisnickoIme(korisnickoIme)!!
        transakcija.kategorija_id = kategorija

        if (korisnikId != 0L) {
            transakcija.transakcijaPrema = korisnikRepozitorij.findById(korisnikId!!).get().korisnickoIme
        }
        transakcijaRepozitorij.save(transakcija)
        if (transakcija.transakcijaPrema != null) {
            obaviPrijenos(racunId, korisnikId, transakcija, locale)
        }
    }

    override fun generatePdfForRange(datumOd: String, datumDo: String): ByteArrayInputStream {
        val entries =
            dohvatiTransakcijeZaKorisnika().stream().filter { entry -> checkEntryRange(entry, datumOd, datumDo) }
                .collect(Collectors.toList<Transakcija>())
        return GeneratorPdfIzvjesca.entriesReport(entries)
    }

    override fun updateExpense(korisnik: Korisnik, expense: Float?, kategorija: Kategorija) {
        korisnik.stanjeRacuna -= expense!!
        createEntry("Expense - " + korisnik.id, Math.abs(expense), korisnik.korisnickoIme, kategorija)
        korisnikRepozitorij.save(korisnik)
    }

    override fun updateIncome(korisnik: Korisnik, income: Float?, kategorija: Kategorija) {
        korisnik.stanjeRacuna += income!!
        createEntry("Income - " + korisnik.id, Math.abs(income), korisnik.korisnickoIme, kategorija)
        korisnikRepozitorij.save(korisnik)
    }

    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }

    override fun dohvatiTransakcijeZaKorisnika(): List<Transakcija> {
        val username = SecurityContextHolder.getContext().authentication.name
        return transakcijaRepozitorij.findAll().stream()
            .filter { entry ->
                !ObjectUtils.isEmpty(entry.transakcijaOd) && entry.transakcijaOd.equals(
                    username
                ) || entry.transakcijaPrema.equals(
                    username
                )
            }
            .collect(Collectors.toList<Transakcija>())
    }

    override fun spremiUvezeneTransakcije(entryList: List<Transakcija>): Int {
        var successfullyWrittenEntries = 0
        for (entry in entryList) {
            if (!checkIfAlreadyExists(entry, dohvatiTransakcijeZaKorisnika())) {
                transakcijaRepozitorij.save(entry)
                successfullyWrittenEntries += 1
            }
        }
        return successfullyWrittenEntries
    }

    override fun dohvatiTransakcije(): List<Transakcija> {
        return transakcijaRepozitorij.findAll().stream()
            .filter { entry ->
                !ObjectUtils.isEmpty(entry.danPlacanja) && entry.danPlacanja!!.isAfter(LocalDateTime.now())
            }
            .collect(Collectors.toList<Transakcija>())
    }

    override fun dohvatiTransakcijeZaMjesec(month: Int): List<Transakcija> {
        return transakcijaRepozitorij.findAll().stream().filter { entry -> checkEntriesForMonth(entry, month) }
            .collect(Collectors.toList<Transakcija>())
    }

    override fun dohvatiTransakcijeZaGodinu(year: Int): List<Transakcija> {
        return transakcijaRepozitorij.findAll().stream().filter { entry -> checkEntriesForYear(entry, year) }
            .collect(Collectors.toList<Transakcija>())
    }

    override fun dohvatiVerifikacijskiToken(verifikacijskiToken: String): VerifikacijskiToken {
        return verifikacijskiTokenRepozitorij.findByToken(verifikacijskiToken)!!
    }

    override fun createPasswordResetTokenForUser(korisnik: Korisnik, token: String) {
        val curTimeInMs = Date().time
        val myToken = Token(token, korisnik)
        myToken.datumIsteka = Date(curTimeInMs + 5 * ONE_MINUTE_IN_MILLIS)
        tokenRepozitorij.save(myToken)
    }

    override fun stvoriVerifikacisjkiTokenZaKorisnika(korisnik: Korisnik, token: String) {
        val myToken = VerifikacijskiToken(token, korisnik)
        verifikacijskiTokenRepozitorij.save(myToken)
    }

    override fun azurirajKorisnika(korisnik: Korisnik): Korisnik {
        return korisnikRepozitorij.save(korisnik)
    }

    override fun spremiKorisnika(korisnik: Korisnik): Korisnik {
        return korisnikRepozitorij.save(korisnik)
    }

    override fun obrisiKorisnika(registerForm: RegisterForm) {
        val korisnik = korisnikRepozitorij.findByKorisnickoIme(registerForm.korisnickoIme!!)
        korisnikRepozitorij.delete(korisnik!!)
    }

    override fun validirajRegistracijskiToken(token: String): String {
        val verificationToken = dohvatiVerifikacijskiToken(token)
        if (ObjectUtils.isEmpty(verificationToken)) {
            return "invalid"
        }
        val cal = Calendar.getInstance()
        return if (verificationToken.datumIsteka!!.time - cal.time.time <= 0) {
            "expired"
        } else "valid"
    }

    override fun validirajTokenZaLozinku(id: Long, token: String): String {
        val passToken = tokenRepozitorij.findByToken(token)
        if (ObjectUtils.isEmpty(passToken) || passToken!!.korisnik!!.id != id) {
            return "invalidToken"
        }

        val cal = Calendar.getInstance()
        if (passToken.datumIsteka!!.time - cal.time.time <= 0) {
            return "expired"
        }

        val user = passToken.korisnik
        val auth = UsernamePasswordAuthenticationToken(
            user, null, Collections
                .singletonList(SimpleGrantedAuthority("CHANGE_PASSWORD_PRIVILEGE"))
        )
        SecurityContextHolder.getContext().authentication = auth
        return "valid"
    }

    override fun promijeniLozinkuKorisniku(korisnik: Korisnik, password: String) {
        korisnik.lozinka = passwordEncoder().encode(password)
        korisnikRepozitorij.save(korisnik)
    }

    override fun findByKorisnickoIme(korisnickoIme: String): Korisnik? {
        return korisnikRepozitorij.findByKorisnickoIme(korisnickoIme)
    }

    override fun findByEmail(email: String): Korisnik? {
        return korisnikRepozitorij.findByEmail(email)
    }

    @Transactional
    override fun spremiRegistriranogKorisnika(registriraniKorisnik: RegisterForm): Korisnik {
        val korisnik = Korisnik()
        korisnik.korisnickoIme = registriraniKorisnik.korisnickoIme!!
        korisnik.ime = registriraniKorisnik.ime
        korisnik.prezime = registriraniKorisnik.prezime
        korisnik.lozinka = passwordEncoder().encode(registriraniKorisnik.lozinka)
        korisnik.email = registriraniKorisnik.email!!
        korisnik.uloga_id = ulogaRepozitorij.findByNaziv("ROLE_USER")
        return korisnikRepozitorij.save(korisnik)
    }

    @Transactional
    override fun azurirajDetaljeKorisnika(korisnik: Korisnik, profileForm: ProfileForm): Korisnik {
        korisnik.ime = profileForm.firstName
        korisnik.prezime = profileForm.lastName
        korisnik.email = profileForm.email!!
        return korisnik
    }


    @Throws(UsernameNotFoundException::class)
    override fun loadUserByUsername(username: String): UserDetails {
        val korisnik = korisnikRepozitorij.findByKorisnickoIme(username)
        return if (korisnik == null) {
            throw UsernameNotFoundException("No korisnik found with korisnickoIme: $username")
        } else {
            User(
                korisnik.korisnickoIme,
                korisnik.lozinka,
                true,
                true,
                true,
                true,
                Collections.singletonList(SimpleGrantedAuthority(korisnik.uloga_id.naziv))
            )
        }
    }

    fun checkIfAlreadyExists(entry: Transakcija, entries: List<Transakcija>): Boolean {
        for (entry1 in entries) {
            if (entry1.kategorija_id!!.naziv == entry.kategorija_id!!.naziv) {
                return true
            }
        }
        return false
    }

    private fun checkEntryRange(entry: Transakcija, dateFrom: String, dateTo: String): Boolean {
        val username = SecurityContextHolder.getContext().authentication.name
        val paymentDate = entry.danPlacanja
        return !ObjectUtils.isEmpty(entry.transakcijaOd) && entry.transakcijaOd
            .equals(username) && isDateBetween(paymentDate, dateFrom, dateTo)
    }

    private fun isDateBetween(paymentDate: LocalDateTime?, dateFrom: String, dateTo: String): Boolean {
        val localDateFrom = LocalDateTime.parse(dateFrom)
        val localDateTo = LocalDateTime.parse(dateTo)
        return paymentDate!!.isAfter(localDateFrom) && paymentDate.isBefore(localDateTo)
    }

    private fun checkEntriesForYear(entry: Transakcija, year: Int): Boolean {
        val username = SecurityContextHolder.getContext().authentication.name
        return entry.danPlacanja!!.year == year && !ObjectUtils.isEmpty(entry.transakcijaOd) && entry.transakcijaOd
            .equals(username)
    }

    private fun checkEntriesForMonth(entry: Transakcija, month: Int): Boolean {
        val username = SecurityContextHolder.getContext().authentication.name
        return entry.danPlacanja!!.monthValue == month && !ObjectUtils.isEmpty(entry.transakcijaOd) && entry.transakcijaOd
            .equals(username)
    }

    @Transactional
    fun createEntry(nazivTransakcije: String, value: Float?, korisnickoIme: String, kategorija: Kategorija) {
        val transakcija = Transakcija(nazivTransakcije)
        transakcija.datumKreiranja = LocalDateTime.now()
        transakcija.vrijednost = value!!.toDouble()
        transakcija.opis = entryDetailsIncome + korisnickoIme
        transakcija.danPlacanja = LocalDateTime.now()
        transakcija.transakcijaOd = korisnickoIme
        transakcija.kategorija_id = kategorija
        transakcija.korisnik = korisnikRepozitorij.findByKorisnickoIme(korisnickoIme)!!
        transakcijaRepozitorij.save(transakcija)
    }

    private fun obaviPrijenos(racunId: Long?, korisnikId: Long?, transakcija: Transakcija, locale: Locale) {
        val korisnikOd = korisnikRepozitorij.findByKorisnickoIme(SecurityContextHolder.getContext().authentication.name)
        val korisnikPrema = korisnikRepozitorij.findByIdOrNull(korisnikId!!)
        val racunPrema = racunRepozitorij.findByIdOrNull(racunId!!)
        if (transakcija.naziv!!.contains("Income") && transakcija.danPlacanja!!.isBefore(LocalDateTime.now())) {
            racunPrema!!.iznos -= transakcija.vrijednost
            korisnikOd!!.stanjeRacuna += transakcija.vrijednost
            notificationService.createNotificationObject(
                poruke.getMessage("label.pay.expense", null, locale) + korisnikOd!!.korisnickoIme,
                korisnikPrema!!
            )
            notificationService.createNotificationObject(
                poruke.getMessage("label.pay.income", null, locale) + korisnikPrema.korisnickoIme,
                korisnikOd
            )
            prijenosRepozitorij.save(Prijenos(transakcija, racunPrema!!))
            korisnikRepozitorij.save(korisnikOd)
            korisnikRepozitorij.save(korisnikPrema)
        } else if (transakcija.naziv!!.contains("Expense") && transakcija.danPlacanja!!.isBefore(LocalDateTime.now())) {
            racunPrema!!.iznos += transakcija.vrijednost
            korisnikOd!!.stanjeRacuna -= transakcija.vrijednost
            notificationService.createNotificationObject(
                poruke.getMessage("label.pay.expense", null, locale) + korisnikPrema!!.korisnickoIme,
                korisnikOd!!
            )
            notificationService.createNotificationObject(
                poruke.getMessage("label.pay.income", null, locale) + korisnikOd.korisnickoIme,
                korisnikPrema
            )
            prijenosRepozitorij.save(Prijenos(transakcija, racunPrema!!))
            korisnikRepozitorij.save(korisnikOd)
            korisnikRepozitorij.save(korisnikPrema)
        }
    }

    private fun preslikajTransakciju(transakcijaDo: Transakcija, transakcijaOd: Transakcija): Transakcija {
        transakcijaDo.naziv = transakcijaOd.naziv
        transakcijaDo.vrijednost = transakcijaOd.vrijednost
        transakcijaDo.danPlacanja = transakcijaOd.danPlacanja
        transakcijaDo.opis = transakcijaOd.opis
        transakcijaDo.transakcijaOd = transakcijaOd.transakcijaOd
        if (!ObjectUtils.isEmpty(transakcijaOd.transakcijaPrema)) {
            transakcijaDo.transakcijaPrema = transakcijaOd.transakcijaPrema
        } else {
            transakcijaDo.transakcijaPrema = ""
        }
        return transakcijaDo
    }
}