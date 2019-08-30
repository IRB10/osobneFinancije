package com.diplomski.osobneFinancije.repozitoriji

import com.diplomski.osobneFinancije.entiteti.Dnevnik
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface DnevnikRepozitorij : JpaRepository<Dnevnik, Long>