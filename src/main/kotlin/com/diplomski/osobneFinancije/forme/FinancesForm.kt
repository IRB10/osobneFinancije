package com.diplomski.osobneFinancije.forme

import com.diplomski.osobneFinancije.entiteti.Kategorija
import com.diplomski.osobneFinancije.entiteti.Korisnik
import com.diplomski.osobneFinancije.entiteti.Racun
import org.springframework.format.annotation.DateTimeFormat
import java.time.LocalDateTime

class FinancesForm {
    var income: Float = 0F

    var expense: Float = 0F

    var value: Float = 0F

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    var obligationDate: LocalDateTime? = null

    var obligationDetails: String? = ""

    var kategorija: Kategorija? = Kategorija()

    var racunKorisnik: String? = ""
}