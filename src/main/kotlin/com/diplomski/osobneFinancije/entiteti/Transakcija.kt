package com.diplomski.osobneFinancije.entiteti

import com.fasterxml.jackson.annotation.JsonIgnore
import java.sql.Timestamp
import java.time.LocalDateTime
import javax.persistence.*

@Entity
data class Transakcija(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    var vrijednost: Double = 0.0,
    var transakcijaOd: String? = "",
    var transakcijaPrema: String? = "",
    var opis: String? = "",
    @JsonIgnore
    var transakcijaProvjerena: Boolean? = false,
    @JsonIgnore
    var datumKreiranja: LocalDateTime? = null,
    var naziv: String? = "",
    var danPlacanja: LocalDateTime? = null
) {
    @JsonIgnore
    @ManyToOne
    var kategorija_id: Kategorija? = Kategorija()

    @JsonIgnore
    @ManyToOne
    var korisnik: Korisnik? = Korisnik()

    @JsonIgnore
    @OneToMany(mappedBy = "transakcija_id")
    val prijenosi: MutableSet<Prijenos> = HashSet()

    constructor(vrijednost: Double, datumKreiranja: LocalDateTime, kategorija_id: Kategorija, korisnik_id: Korisnik) : this(
        vrijednost = vrijednost,
        datumKreiranja = datumKreiranja
    ) {
        this.kategorija_id = kategorija_id
        this.korisnik = korisnik_id
    }

    constructor(naziv: String?) : this(){
        this.naziv = naziv
    }
}