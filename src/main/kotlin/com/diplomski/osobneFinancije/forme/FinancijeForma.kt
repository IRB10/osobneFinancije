package com.diplomski.osobneFinancije.forme

import com.diplomski.osobneFinancije.entiteti.Kategorija
import com.diplomski.osobneFinancije.entiteti.Korisnik
import com.diplomski.osobneFinancije.entiteti.Racun
import org.springframework.format.annotation.DateTimeFormat
import java.time.LocalDateTime

class FinancijeForma {
    var prihod: Float = 0F

    var troskovi: Float = 0F

    var vrijednost: Float = 0F

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    var datumObveze: LocalDateTime? = null

    var detaljiObveze: String? = ""

    var kategorija: Kategorija? = Kategorija()

    var racunKorisnik: String? = ""
}