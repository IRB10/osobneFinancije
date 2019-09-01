package com.diplomski.osobneFinancije.entiteti

import org.codehaus.jackson.annotate.JsonIgnore
import javax.persistence.*

@Entity
data class Kategorija(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    var naziv: String = "",
    var opis: String? = null
) {
    @JsonIgnore
    @OneToMany(mappedBy = "kategorija_id")
    val transakcije: MutableSet<Transakcija> = HashSet()
}