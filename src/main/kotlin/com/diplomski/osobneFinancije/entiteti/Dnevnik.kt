package com.diplomski.osobneFinancije.entiteti

import java.sql.Timestamp
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id

@Entity
data class Dnevnik(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    val korisnik: String = "",
    val akcija: String? = null,
    val vrijeme: Timestamp? = null
)