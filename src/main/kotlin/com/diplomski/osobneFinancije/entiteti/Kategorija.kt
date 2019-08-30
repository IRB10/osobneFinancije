package com.diplomski.osobneFinancije.entiteti

import javax.persistence.*

@Entity
data class Kategorija(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    var naziv: String = "",
    var opis: String? = null
) {
    @OneToMany(mappedBy = "kategorija_id")
    val transakcije: MutableSet<Transakcija> = HashSet()
}