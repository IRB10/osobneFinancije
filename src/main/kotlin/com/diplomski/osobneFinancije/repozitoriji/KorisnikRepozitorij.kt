package com.diplomski.osobneFinancije.repozitoriji

import com.diplomski.osobneFinancije.entiteti.Korisnik
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface KorisnikRepozitorij : JpaRepository<Korisnik, Long> {
    fun findByKorisnickoIme(korisnickoIme: String): Korisnik?
    fun findByEmail(email: String): Korisnik?
}