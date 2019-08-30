package com.diplomski.osobneFinancije.repozitoriji

import com.diplomski.osobneFinancije.entiteti.Racun
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface RacunRepozitorij : JpaRepository<Racun, Long>