package com.diplomski.osobneFinancije.repozitoriji

import com.diplomski.osobneFinancije.entiteti.Token
import org.springframework.data.jpa.repository.JpaRepository

interface TokenRepozitorij : JpaRepository<Token, Long>{
    fun findByToken(token: String): Token?
}