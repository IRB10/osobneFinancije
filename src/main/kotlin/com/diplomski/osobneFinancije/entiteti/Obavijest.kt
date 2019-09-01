package com.diplomski.osobneFinancije.entiteti

import com.fasterxml.jackson.annotation.JsonIgnore
import java.util.*
import javax.persistence.*


@Entity
data class Obavijest(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = 0L,

    var poruka: String? = null,

    var kreirano: Date? = null,

    var procitano: Boolean = false
) {
    @JsonIgnore
    @ManyToOne
    lateinit var korisnik: Korisnik

    constructor(poruka: String?, kreirano: Date?, korisnik: Korisnik) : this(){
        this.poruka  = poruka
        this.kreirano  = kreirano
        this.korisnik  = korisnik
    }
}