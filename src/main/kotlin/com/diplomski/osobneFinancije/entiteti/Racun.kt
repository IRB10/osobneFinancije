package com.diplomski.osobneFinancije.entiteti

import com.fasterxml.jackson.annotation.JsonIgnore
import java.util.*
import javax.persistence.*


@Entity
data class Racun(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    var iznos: Double = 0.0,
    var opis: String? = "",
    var aktivan: Boolean? = false,
    var vrstaRacuna: String? = ""
) {
    @JsonIgnore
    @OneToMany(mappedBy = "racun_id")
    val prijenosi: MutableSet<Prijenos> = HashSet()

    @JsonIgnore
    @ManyToMany(fetch = FetchType.LAZY, cascade = [CascadeType.PERSIST, CascadeType.MERGE], mappedBy = "racuni")
    var korisnici: MutableSet<Korisnik> = HashSet()
}