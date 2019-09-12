package com.diplomski.osobneFinancije.repozitoriji

import com.diplomski.osobneFinancije.entiteti.Korisnik
import com.diplomski.osobneFinancije.entiteti.VerifikacijskiToken
import org.springframework.data.jpa.repository.JpaRepository

interface VerifikacijskiTokenRepozitorij : JpaRepository<VerifikacijskiToken, Long> {
    fun findByToken(token: String): VerifikacijskiToken?
    fun findByKorisnik(korisnik: Korisnik): VerifikacijskiToken
}