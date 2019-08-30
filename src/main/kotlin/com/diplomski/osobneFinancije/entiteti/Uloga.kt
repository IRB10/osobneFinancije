package com.diplomski.osobneFinancije.entiteti

import javax.persistence.*

@Entity
data class Uloga(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    val naziv: String = ""
) {
    @OneToMany(mappedBy = "uloga_id")
    val korisnici: MutableSet<Korisnik> = HashSet()
}