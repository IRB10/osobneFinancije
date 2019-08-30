package com.diplomski.osobneFinancije.repozitoriji

import com.diplomski.osobneFinancije.entiteti.Transakcija
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface TransakcijaRepozitorij : JpaRepository<Transakcija, Long> {
    fun findByNaziv(naziv: String): Transakcija
}