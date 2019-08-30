package com.diplomski.osobneFinancije.entiteti

import javax.persistence.*

@Entity
data class Prijenos(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0
) {
    @ManyToOne
    lateinit var transakcija_id: Transakcija

    @ManyToOne
    lateinit var racun_id: Racun

    constructor(transakcija_id: Transakcija, racun_id: Racun) : this() {
        this.transakcija_id = transakcija_id
        this.racun_id = racun_id
    }
}