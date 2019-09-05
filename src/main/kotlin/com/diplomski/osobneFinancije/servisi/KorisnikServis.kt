package com.diplomski.osobneFinancije.servisi

import com.diplomski.osobneFinancije.entiteti.*
import com.diplomski.osobneFinancije.forme.FinancesForm
import com.diplomski.osobneFinancije.forme.ProfileForm
import com.diplomski.osobneFinancije.forme.RacunForma
import com.diplomski.osobneFinancije.forme.RegisterForm
import org.springframework.security.core.userdetails.UserDetailsService
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.io.ByteArrayInputStream
import java.util.*

interface KorisnikServis : UserDetailsService {
    fun findByKorisnickoIme(korisnickoIme: String): Korisnik?
    fun findByEmail(email: String): Korisnik?
    fun spremiRegistriranogKorisnika(registriraniKorisnik: RegisterForm): Korisnik
    fun azurirajDetaljeKorisnika(korisnik: Korisnik, profileForm: ProfileForm): Korisnik
    fun promijeniLozinkuKorisniku(korisnik: Korisnik, password: String)
    fun validirajTokenZaLozinku(id: Long, token: String): String
    fun validirajRegistracijskiToken(token: String): String
    fun azurirajKorisnika(korisnik: Korisnik): Korisnik
    fun spremiKorisnika(korisnik: Korisnik): Korisnik
    fun obrisiKorisnika(registerForm: RegisterForm)
    fun stvoriVerifikacisjkiTokenZaKorisnika(korisnik: Korisnik, token: String)
    fun createPasswordResetTokenForUser(korisnik: Korisnik, token: String)
    fun dohvatiVerifikacijskiToken(verifikacijskiToken: String): VerifikacijskiToken
    fun dohvatiTransakcijeZaKorisnika(): List<Transakcija>
    fun spremiUvezeneTransakcije(entryList: List<Transakcija>): Int
    fun dohvatiTransakcije(): List<Transakcija>
    fun dohvatiTransakcijeZaMjesec(month: Int): List<Transakcija>
    fun dohvatiTransakcijeZaGodinu(year: Int): List<Transakcija>
    fun updateObligation(
        financesForm: FinancesForm,
        korisnickoIme: String,
        obligationType: String,
        kategorija: Kategorija?,
        korisnikId: Long?,
        racunId: Long?,
        locale: Locale
    )

    fun generatePdfForRange(datumOd: String, datumDo: String): ByteArrayInputStream
    fun updateExpense(korisnik: Korisnik, expense: Float?, kategorija: Kategorija)
    fun updateIncome(korisnik: Korisnik, income: Float?, kategorija: Kategorija)
    fun dohvatiRacuneKorisnika(): List<Racun>
    fun kreirajRacun(racunForma: RacunForma)
    fun deaktivirajRacun(racun: Racun)
    fun obrisiRacun(racun: Racun)
    fun dohvatiOstaleKorisnike(): List<Korisnik?>
    fun dohvatiSveRacune(): List<Racun>
    fun dohvatiSveTransakcije(): List<Transakcija>
    fun spremiTransakciju(transakcija: Transakcija)
    fun dohvatiTransakcijeZaKorisnika(username: String): List<Transakcija>
    fun azurirajTransakciju(transakcija: Transakcija)
    fun dohvatiSveRacuneDostupneKorisniku(): List<Racun>

    fun dohvatiKorisnikaReaktivno(korisnickoIme: String): Mono<Korisnik>
    fun dohvatiSveKorisnikeReaktivno(): Flux<Korisnik>
    fun dodajKorisnikaReaktivno(korisnik: Korisnik): Mono<Korisnik?>
    fun dohvatiSveTransakcijeReaktivnoZaGodinu(year: Int): Flux<Transakcija>
    fun dohvatiSveTransakcijeReaktivno(): Flux<Transakcija>
}