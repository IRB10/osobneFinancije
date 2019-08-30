package com.diplomski.osobneFinancije.entiteti

import com.diplomski.osobneFinancije.forme.RacunForma
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
    @OneToMany(mappedBy = "racun_id")
    val prijenosi: MutableSet<Prijenos> = HashSet()

    @ManyToMany(fetch = FetchType.LAZY, cascade = [CascadeType.PERSIST, CascadeType.MERGE], mappedBy = "racuni")
    var korisnici: MutableSet<Korisnik> = HashSet()
}