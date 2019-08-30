package com.diplomski.osobneFinancije.entiteti

import java.util.*
import javax.persistence.*


@Entity
data class Token(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    var token: String = ""
) {
    constructor(token: String, korisnik: Korisnik) : this() {

        this.token = token
        this.korisnik = korisnik
        this.datumIsteka = izracunajDatumIsteka()
    }

    private fun izracunajDatumIsteka(): Date {
        val cal = Calendar.getInstance()
        cal.timeInMillis = Date().time
        cal.add(Calendar.MINUTE, trajanjeTokena)
        return Date(cal.time.time)
    }

    @OneToOne(targetEntity = Korisnik::class, fetch = FetchType.EAGER)
    @JoinColumn(nullable = false, name = "korisnik_id")
    var korisnik: Korisnik? = null

    @Column(name = "expiry_date", columnDefinition = "DATETIME")
    var datumIsteka: Date? = null

    companion object {
        const val trajanjeTokena = 60 * 24
    }
}