package com.diplomski.osobneFinancije.servisi

import com.diplomski.osobneFinancije.entiteti.*
import com.diplomski.osobneFinancije.forme.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.security.core.userdetails.UserDetailsService
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.io.ByteArrayInputStream
import java.util.*

interface KorisnikServis : UserDetailsService {
    fun pronadiPoKorisnickomImenu(korisnickoIme: String): Korisnik?
    fun pronadiPoEmailu(email: String): Korisnik?
    fun spremiRegistriranogKorisnika(registriraniKorisnik: RegistracijaForma): Korisnik
    fun azurirajDetaljeKorisnika(korisnik: Korisnik, profilForma: ProfilForma): Korisnik
    fun promijeniLozinkuKorisniku(korisnik: Korisnik, password: String)
    fun validirajTokenZaLozinku(id: Long, token: String): String
    fun validirajRegistracijskiToken(token: String): String
    fun azurirajKorisnika(korisnik: Korisnik): Korisnik
    fun spremiKorisnika(korisnik: Korisnik): Korisnik
    fun obrisiKorisnika(registracijaForma: RegistracijaForma)
    fun stvoriVerifikacisjkiTokenZaKorisnika(korisnik: Korisnik, token: String)
    fun kreirajTokenZaObnovuLozinkeKorisniku(korisnik: Korisnik, token: String)
    fun dohvatiVerifikacijskiToken(verifikacijskiToken: String): VerifikacijskiToken
    fun dohvatiTransakcijeZaKorisnika(): List<Transakcija>
    fun spremiUvezeneTransakcije(entryList: List<Transakcija>): Int
    fun dohvatiTransakcije(): List<Transakcija>
    fun dohvatiTransakcijeZaMjesec(month: Int): List<Transakcija>
    fun dohvatiTransakcijeZaGodinu(year: Int): List<Transakcija>
    fun azurirajTransakciju(
        financijeForma: FinancijeForma,
        korisnickoIme: String,
        obligationType: String,
        kategorija: Kategorija?,
        korisnikId: Long?,
        racunId: Long?,
        locale: Locale
    )

    fun generirajPdfZaRasponDatuma(datumOd: String, datumDo: String, locale: Locale): ByteArrayInputStream
    fun azurirajTrosak(korisnik: Korisnik, expense: Float?, kategorija: Kategorija)
    fun azurirajPrihod(korisnik: Korisnik, income: Float?, kategorija: Kategorija)
    fun dohvatiRacuneKorisnika(): List<Racun>
    fun kreirajRacun(racunForma: RacunForma)
    fun deaktivirajRacun(racun: Racun)
    fun obrisiRacun(racun: Racun)
    fun dohvatiOstaleKorisnike(): List<Korisnik?>
    fun dohvatiSveRacune(): List<Racun>
    fun dohvatiSveTransakcije(): List<Transakcija>
    fun spremiTransakciju(transakcija: Transakcija)
    fun spremiTransakcijuAJAX(transakcija: FinancijeForma)
    fun dohvatiTransakcijeZaKorisnika(username: String): List<Transakcija>
    fun azurirajTransakciju(transakcija: Transakcija)
    fun dohvatiSveRacuneDostupneKorisniku(): List<Racun>
    fun dohvatiSveTransakcijeZaDan(): List<Transakcija>
    fun dohvatiSveKorisnike(): List<Korisnik>

    fun dohvatiKorisnikaReaktivno(korisnickoIme: String): Mono<Korisnik>
    fun dohvatiSveKorisnikeReaktivno(): Flux<Korisnik>
    fun dodajKorisnikaReaktivno(korisnik: Korisnik): Mono<Korisnik?>
    fun dohvatiSveTransakcijeReaktivnoZaGodinu(year: Int): Flux<Transakcija>
    fun dohvatiSveTransakcijeReaktivno(): Flux<Transakcija>
    fun staraLozinkuKorisnikaValidna(korisnickoIme: String, staraLozinka: String?): Boolean
    fun dodajKategoriju(kategorijaForma: KategorijaForma)

    fun stranicenjeTransakcija(pageable:Pageable, transakcije : List<Transakcija>) : Page<Transakcija>
}