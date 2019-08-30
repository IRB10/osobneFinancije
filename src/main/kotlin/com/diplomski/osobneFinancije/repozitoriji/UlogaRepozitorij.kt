package com.diplomski.osobneFinancije.repozitoriji

import com.diplomski.osobneFinancije.entiteti.Uloga
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UlogaRepozitorij : JpaRepository<Uloga, Long>{
    fun findByNaziv(naziv: String): Uloga
}