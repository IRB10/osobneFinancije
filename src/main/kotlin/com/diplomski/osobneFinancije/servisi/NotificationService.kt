package com.diplomski.osobneFinancije.servisi

import com.diplomski.osobneFinancije.entiteti.Korisnik
import com.diplomski.osobneFinancije.entiteti.Obavijest

interface NotificationService {
    fun save(obavijest: Obavijest): Obavijest?

    fun findByUser(korisnik: Korisnik): Obavijest?

    fun findAllByUser(korisnik: Korisnik): List<Obavijest>?

    fun createNotificationObject(poruka: String, korisnik: Korisnik): Obavijest

    fun findByUserAndId(korisnik: Korisnik, obavijestId: Int?): Obavijest?

    fun findAllReadByUser(korisnik: Korisnik): List<Obavijest>?

    fun setAsRead(listaObavijesti: List<Obavijest>)
}