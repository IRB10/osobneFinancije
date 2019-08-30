package com.diplomski.osobneFinancije.forme

import com.diplomski.osobneFinancije.entiteti.Korisnik
import com.diplomski.osobneFinancije.entiteti.Racun
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull
import kotlin.streams.toList

class ProfileForm {

    var username: String? = null

    @NotNull
    @NotEmpty
    var firstName: String? = null

    @NotNull
    @NotEmpty
    var lastName: String? = null

    @NotNull
    @NotEmpty
    var email: String? = null

    var balance: Double? = null

    constructor() {

    }

    constructor(korisnik: Korisnik?) {
        this.username = korisnik!!.korisnickoIme
        this.firstName = korisnik.ime
        this.lastName = korisnik.prezime
        this.email = korisnik.email
        this.balance = korisnik.stanjeRacuna + zbrojiIznosSvihRacuna(korisnik.racuni.stream().toList())
    }

    fun zbrojiIznosSvihRacuna(racuni: List<Racun>): Double {
        var ukupno = 0.0
        for (racun in racuni) {
            ukupno += racun.iznos
        }
        return ukupno
    }
}