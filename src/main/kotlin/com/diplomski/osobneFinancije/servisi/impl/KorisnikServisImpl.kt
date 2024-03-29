package com.diplomski.osobneFinancije.servisi.impl

import com.amazonaws.services.lambda.AWSLambdaClientBuilder
import com.amazonaws.services.lambda.invoke.LambdaInvokerFactory
import com.diplomski.osobneFinancije.entiteti.*
import com.diplomski.osobneFinancije.forme.*
import com.diplomski.osobneFinancije.repozitoriji.*
import com.diplomski.osobneFinancije.servisi.FinancijeServis
import com.diplomski.osobneFinancije.servisi.KorisnikServis
import com.diplomski.osobneFinancije.servisi.ObavijestiServis
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
import org.springframework.transaction.support.TransactionTemplate
import org.springframework.util.ObjectUtils
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.scheduler.Scheduler
import java.io.ByteArrayInputStream
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*
import java.util.stream.Collectors
import kotlin.collections.ArrayList

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
    private val obavijestiServis: ObavijestiServis,
    private val kategorijaRepozitorij: KategorijaRepozitorij,
    @param:Qualifier("messageSource") private val poruke: MessageSource,
    private val transactionTemplate: TransactionTemplate,
    @param:Qualifier("jdbcScheduler") private val jdbcScheduler: Scheduler
) : KorisnikServis {
    override fun dohvatiSveKorisnike(): List<Korisnik> {
        return korisnikRepozitorij.findAll()
    }

    override fun spremiTransakcijuAJAX(transakcija: FinancijeForma) {
        val transakcijaBaza = Transakcija()
        transakcijaBaza.transakcijaOd = SecurityContextHolder.getContext().authentication.name
        transakcijaBaza.korisnik =
            korisnikRepozitorij.findByKorisnickoIme(SecurityContextHolder.getContext().authentication.name)
        transakcijaBaza.transakcijaPrema =
            korisnikRepozitorij.findById(transakcija.transakcijaPrema.toLong()).get().korisnickoIme
        transakcijaBaza.kategorija_id = kategorijaRepozitorij.findById(transakcija.kategorija_id!!.toLong()).get()
        transakcijaBaza.datumKreiranja = LocalDateTime.now()
        transakcijaBaza.opis = transakcija.detaljiObveze
        transakcijaBaza.vrijednost = transakcija.vrijednost.toDouble()
        transakcijaBaza.naziv = transakcija.naziv
        transakcijaBaza.danPlacanja = LocalDateTime.parse(transakcija.danPlacanja)
        transakcijaRepozitorij.save(transakcijaBaza)
    }

    override fun dodajKategoriju(kategorijaForma: KategorijaForma) {
        val kategorija = Kategorija()
        kategorija.naziv = kategorijaForma.naziv
        kategorija.opis = kategorijaForma.opis
        kategorijaRepozitorij.save(kategorija)
    }

    override fun staraLozinkuKorisnikaValidna(korisnickoIme: String, staraLozinka: String?): Boolean {
        var korisnik = korisnikRepozitorij.findByKorisnickoIme(korisnickoIme)
        return passwordEncoder().matches(staraLozinka, korisnik!!.lozinka)
    }

    override fun dohvatiSveTransakcijeZaDan(): List<Transakcija> {
        return transakcijaRepozitorij.findAll().stream().filter { entry -> checkEntriesForDay(entry) }
            .collect(Collectors.toList<Transakcija>())
    }

    override fun dohvatiSveTransakcijeReaktivno(): Flux<Transakcija> {
        val defer = Flux.defer { Flux.fromIterable(this.transakcijaRepozitorij.findAll()) }
        return defer.subscribeOn(jdbcScheduler)
    }

    override fun dohvatiSveTransakcijeReaktivnoZaGodinu(year: Int): Flux<Transakcija> {
        val listaTransakcija = dohvatiTransakcijeZaGodinu(year)
        val defer = Flux.defer { Flux.fromIterable(listaTransakcija) }
        return defer.subscribeOn(jdbcScheduler)
    }

    override fun dohvatiKorisnikaReaktivno(korisnickoIme: String): Mono<Korisnik> {
        return Mono
            .defer { Mono.just(this.korisnikRepozitorij.findByKorisnickoIme(korisnickoIme)!!) }
            .subscribeOn(jdbcScheduler)
    }

    override fun dohvatiSveKorisnikeReaktivno(): Flux<Korisnik> {
        val defer = Flux.defer { Flux.fromIterable(this.korisnikRepozitorij.findAll()) }
        return defer.subscribeOn(jdbcScheduler)
    }

    override fun dodajKorisnikaReaktivno(korisnik: Korisnik): Mono<Korisnik?> {
        return Mono.fromCallable {
            transactionTemplate.execute {
                val spremljeniKorisnik = korisnikRepozitorij.save(korisnik)
                spremljeniKorisnik
            }
        }.subscribeOn(jdbcScheduler)
    }

    val financijeServis: FinancijeServis = LambdaInvokerFactory.builder()
        .lambdaClient(AWSLambdaClientBuilder.defaultClient())
        .build(FinancijeServis::class.java)

    override fun dohvatiSveRacuneDostupneKorisniku(): List<Racun> {
        val listaRacuna = ArrayList<Racun>()
        for (racun in racunRepozitorij.findAll()) {
            if (!racun.korisnici.contains(korisnikRepozitorij.findByKorisnickoIme(SecurityContextHolder.getContext().authentication.name))) {
                listaRacuna.add(racun)
            }
        }
        return listaRacuna
    }

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
    override fun azurirajTransakciju(
        financijeForma: FinancijeForma,
        korisnickoIme: String,
        obligationType: String,
        kategorija: Kategorija?,
        korisnikId: Long?,
        racunId: Long?,
        locale: Locale
    ) {
        val transakcija = Transakcija("Obligation - " + obligationType + " : " + financijeForma.datumObveze)
        transakcija.datumKreiranja = LocalDateTime.now()
        transakcija.vrijednost = financijeForma.vrijednost.toDouble()
        transakcija.opis = financijeForma.detaljiObveze
        transakcija.danPlacanja = financijeForma.datumObveze
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

    override fun generirajPdfZaRasponDatuma(datumOd: String, datumDo: String): ByteArrayInputStream {
        val entries =
            dohvatiTransakcijeZaKorisnika().stream().filter { entry -> checkEntryRange(entry, datumOd, datumDo) }
                .collect(Collectors.toList<Transakcija>())
        return GeneratorPdfIzvjesca.entriesReport(entries)
    }

    override fun azurirajTrosak(korisnik: Korisnik, expense: Float?, kategorija: Kategorija) {
        val response = financijeServis.lambdaOutput(LambdaInput(korisnik.stanjeRacuna, expense!!.toDouble(), "-"))
        korisnik.stanjeRacuna = response.result
        createEntry(
            "Expense - " + korisnik.id + " - " + korisnik.id + LocalDateTime.now(),
            Math.abs(expense),
            korisnik.korisnickoIme,
            kategorija
        )
        korisnikRepozitorij.save(korisnik)
    }

    override fun azurirajPrihod(korisnik: Korisnik, income: Float?, kategorija: Kategorija) {
        val response = financijeServis.lambdaOutput(LambdaInput(korisnik.stanjeRacuna, income!!.toDouble(), "+"))
        korisnik.stanjeRacuna = response.result
        createEntry(
            "Income - " + korisnik.id + " - " + LocalDateTime.now(),
            Math.abs(income),
            korisnik.korisnickoIme,
            kategorija
        )
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

    override fun kreirajTokenZaObnovuLozinkeKorisniku(korisnik: Korisnik, token: String) {
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

    override fun obrisiKorisnika(registracijaForma: RegistracijaForma) {
        val korisnik = korisnikRepozitorij.findByKorisnickoIme(registracijaForma.korisnickoIme!!)
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

    override fun pronadiPoKorisnickomImenu(korisnickoIme: String): Korisnik? {
        return korisnikRepozitorij.findByKorisnickoIme(korisnickoIme)
    }

    override fun pronadiPoEmailu(email: String): Korisnik? {
        return korisnikRepozitorij.findByEmail(email)
    }

    @Transactional
    override fun spremiRegistriranogKorisnika(registriraniKorisnik: RegistracijaForma): Korisnik {
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
    override fun azurirajDetaljeKorisnika(korisnik: Korisnik, profilForma: ProfilForma): Korisnik {
        korisnik.ime = profilForma.ime
        korisnik.prezime = profilForma.prezime
        korisnik.email = profilForma.email!!
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
            if (entry1.naziv == entry.naziv) {
                return true
            }
        }
        return false
    }

    private fun checkEntryRange(entry: Transakcija, dateFrom: String, dateTo: String): Boolean {
        val username = SecurityContextHolder.getContext().authentication.name
        val paymentDate = entry.danPlacanja!!.toLocalDate()
        return !ObjectUtils.isEmpty(entry.transakcijaOd) && entry.transakcijaOd
            .equals(username) && isDateBetween(paymentDate, dateFrom, dateTo)
    }

    private fun isDateBetween(paymentDate: LocalDate?, dateFrom: String, dateTo: String): Boolean {
        val localDateFrom = LocalDate.parse(dateFrom)
        val localDateTo = LocalDate.parse(dateTo)
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

    private fun checkEntriesForDay(entry: Transakcija): Boolean {
        return entry.danPlacanja!!.toLocalDate() == LocalDate.now().minusDays(1)
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
            obavijestiServis.stvoriObavijestObjekt(
                poruke.getMessage("label.pay.troskovi", null, locale) + korisnikOd!!.korisnickoIme,
                korisnikPrema!!
            )
            obavijestiServis.stvoriObavijestObjekt(
                poruke.getMessage("label.pay.income", null, locale) + korisnikPrema.korisnickoIme,
                korisnikOd
            )
            prijenosRepozitorij.save(Prijenos(transakcija, racunPrema!!))
            korisnikRepozitorij.save(korisnikOd)
            korisnikRepozitorij.save(korisnikPrema)
        } else if (transakcija.naziv!!.contains("Expense") && transakcija.danPlacanja!!.isBefore(LocalDateTime.now())) {
            racunPrema!!.iznos += transakcija.vrijednost
            korisnikOd!!.stanjeRacuna -= transakcija.vrijednost
            obavijestiServis.stvoriObavijestObjekt(
                poruke.getMessage("label.pay.troskovi", null, locale) + korisnikPrema!!.korisnickoIme,
                korisnikOd!!
            )
            obavijestiServis.stvoriObavijestObjekt(
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