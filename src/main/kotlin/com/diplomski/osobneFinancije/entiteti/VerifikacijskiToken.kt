package com.diplomski.osobneFinancije.entiteti

import java.util.*
import javax.persistence.*


@Suppress("NAME_SHADOWING")
@Entity
data class VerifikacijskiToken(

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    var id: Long? = null,

    var token: String? = null,

    var datumIsteka: Date? = null
) {

    @OneToOne(targetEntity = Korisnik::class, fetch = FetchType.EAGER)
    @JoinColumn(nullable = false, name = "korisnik_id", foreignKey = ForeignKey(name = "FK_VERIFY_USER"))
    var korisnik: Korisnik? = null

    constructor(token: String) : this() {

        this.token = token
        this.datumIsteka = izracunajDatumIsteka()
    }

    constructor(token: String, user: Korisnik) : this() {

        this.token = token
        this.korisnik = user
        this.datumIsteka = izracunajDatumIsteka()
    }

    private fun izracunajDatumIsteka(): Date {
        val cal = Calendar.getInstance()
        cal.timeInMillis = Date().time
        cal.add(Calendar.MINUTE, trajanjeTokena)
        return Date(cal.time.time)
    }

    fun azurirajToken(token: String) {
        this.token = token
        this.datumIsteka = izracunajDatumIsteka()
    }

    override fun hashCode(): Int {
        val prime = 31
        var result = 1
        result = prime * result + if (datumIsteka == null) 0 else datumIsteka!!.hashCode()
        result = prime * result + if (token == null) 0 else token!!.hashCode()
        result = prime * result + if (korisnik == null) 0 else korisnik!!.hashCode()
        return result
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other == null) {
            return false
        }
        if (javaClass != other.javaClass) {
            return false
        }
        val other = other as VerifikacijskiToken?
        if (datumIsteka == null) {
            if (other!!.datumIsteka != null) {
                return false
            }
        } else if (datumIsteka!! != other!!.datumIsteka) {
            return false
        }
        if (token == null) {
            if (other.token != null) {
                return false
            }
        } else if (token != other.token) {
            return false
        }
        if (korisnik == null) {
            if (other.korisnik != null) {
                return false
            }
        } else if (korisnik!! != other.korisnik) {
            return false
        }
        return true
    }

    override fun toString(): String {
        val builder = StringBuilder()
        builder.append("Token [String=").append(token).append("]").append("[Expires").append(datumIsteka).append("]")
        return builder.toString()
    }

    companion object {
        val trajanjeTokena = 60 * 24
    }
}