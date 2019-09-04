package com.diplomski.osobneFinancije.entiteti

import com.fasterxml.jackson.annotation.JsonIgnore
import javax.persistence.*

@Entity
data class Prijenos(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0
) {
    @JsonIgnore
    @ManyToOne
    lateinit var transakcija_id: Transakcija

    @JsonIgnore
    @ManyToOne
    lateinit var racun_id: Racun

    constructor(transakcija_id: Transakcija, racun_id: Racun) : this() {
        this.transakcija_id = transakcija_id
        this.racun_id = racun_id
    }
}