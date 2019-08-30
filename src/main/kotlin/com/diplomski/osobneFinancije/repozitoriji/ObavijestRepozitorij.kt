package com.diplomski.osobneFinancije.repozitoriji

import com.diplomski.osobneFinancije.entiteti.Korisnik
import com.diplomski.osobneFinancije.entiteti.Obavijest
import org.springframework.data.jpa.repository.JpaRepository

interface ObavijestRepozitorij : JpaRepository<Obavijest, Long>{
    fun findByKorisnik(korisnik: Korisnik): Obavijest?
    fun findByKorisnikAndId(korisnik: Korisnik, obavijestId: Int?): Obavijest
}