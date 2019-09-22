package com.diplomski.osobneFinancije.servisi

import com.diplomski.osobneFinancije.entiteti.Korisnik
import com.diplomski.osobneFinancije.entiteti.Obavijest
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface ObavijestiServis {
    fun spremiObavijest(obavijest: Obavijest): Obavijest?

    fun pronadiZaKorisnika(korisnik: Korisnik): Obavijest?

    fun pronadiSveZaKorisnika(korisnik: Korisnik): List<Obavijest>?

    fun stvoriObavijestObjekt(poruka: String, korisnik: Korisnik): Obavijest

    fun pronadiPoKorisnikuIIdu(korisnik: Korisnik, obavijestId: Int?): Obavijest?

    fun pronadiSveProcitanePoKorisniku(korisnik: Korisnik): List<Obavijest>?

    fun oznaciKaoProcitano(listaObavijesti: List<Obavijest>)
    fun stranicenjeObavijesti(pageable: Pageable, obavijesti : List<Obavijest>) : Page<Obavijest>
}