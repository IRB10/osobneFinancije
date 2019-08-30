package com.diplomski.osobneFinancije.entiteti

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import org.hibernate.annotations.NaturalId
import java.sql.Timestamp
import java.util.*
import javax.persistence.*
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size


@Entity
data class Korisnik(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = 0L,
    var ime: String? = null,
    var prezime: String? = null,
    @Column(name = "email", nullable = false)
    var email: String = "",
    @NaturalId
    @Column(name = "korisnicko_ime", unique = true, nullable = false)
    @NotNull
    @Size(min = 3, max = 255)
    var korisnickoIme: String = email,
    @Size(min = 8)
    @get:JsonIgnore
    @set:JsonProperty
    var lozinka: String? = null,
    val datum_prijave: Timestamp? = null,
    var aktivan : Boolean = false,
    var stanjeRacuna : Double = 0.0
) {
    @OneToMany(mappedBy = "korisnik")
    val transakcije: MutableSet<Transakcija> = HashSet()

    @ManyToOne
    lateinit var uloga_id: Uloga

    @ManyToMany(fetch = FetchType.LAZY, cascade = [CascadeType.PERSIST, CascadeType.MERGE])
    @JoinTable(
        name = "korisnici_racuni",
        joinColumns = [JoinColumn(name = "korisnik_id")],
        inverseJoinColumns = [JoinColumn(name = "racun_id")]
    )
    val racuni: MutableSet<Racun> = HashSet()

    @OneToMany(mappedBy = "korisnik")
    val obavijesti: MutableSet<Obavijest> = HashSet()
}