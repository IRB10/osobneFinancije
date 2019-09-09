package com.diplomski.osobneFinancije.forme

import com.diplomski.osobneFinancije.entiteti.Korisnik
import com.diplomski.osobneFinancije.entiteti.Racun
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull
import kotlin.streams.toList

class ProfilForma {

    var korisnickoIme: String? = null

    @NotNull
    @NotEmpty
    var ime: String? = null

    @NotNull
    @NotEmpty
    var prezime: String? = null

    @NotNull
    @NotEmpty
    var email: String? = null

    var stanjeRacuna: Double? = null

    constructor() {

    }

    constructor(korisnik: Korisnik?) {
        this.korisnickoIme = korisnik!!.korisnickoIme
        this.ime = korisnik.ime
        this.prezime = korisnik.prezime
        this.email = korisnik.email
        this.stanjeRacuna = korisnik.stanjeRacuna + zbrojiIznosSvihRacuna(korisnik.racuni.stream().toList())
    }

    fun zbrojiIznosSvihRacuna(racuni: List<Racun>): Double {
        var ukupno = 0.0
        for (racun in racuni) {
            ukupno += racun.iznos
        }
        return ukupno
    }
}