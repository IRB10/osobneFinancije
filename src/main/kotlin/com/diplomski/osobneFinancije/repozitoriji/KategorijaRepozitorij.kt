package com.diplomski.osobneFinancije.repozitoriji

import com.diplomski.osobneFinancije.entiteti.Kategorija
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface KategorijaRepozitorij : JpaRepository<Kategorija, Long> {
    fun findByNaziv(naziv: String): Kategorija
}