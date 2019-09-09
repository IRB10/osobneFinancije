package com.diplomski.osobneFinancije.servisi.impl

import com.diplomski.osobneFinancije.entiteti.Korisnik
import com.diplomski.osobneFinancije.entiteti.Obavijest
import com.diplomski.osobneFinancije.repozitoriji.ObavijestRepozitorij
import com.diplomski.osobneFinancije.servisi.ObavijestiServis
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.*


@Service
class ObavijestiServisImpl(private val obavijestRepozitorij: ObavijestRepozitorij) : ObavijestiServis {

    companion object {

        private val logger = LoggerFactory.getLogger(ObavijestiServisImpl::class.java)
    }

    override fun spremiObavijest(obavijest: Obavijest): Obavijest? {
        return try {
            obavijestRepozitorij.save(obavijest)
        } catch (e: Exception) {
            logger.error("Exception occur while spremiObavijest Notification ", e)
            null
        }
    }

    override fun pronadiZaKorisnika(korisnik: Korisnik): Obavijest? {
        return try {
            obavijestRepozitorij.findByKorisnik(korisnik)
        } catch (e: Exception) {
            logger.error("Exception occur while fetch Notification by User ", e)
            null
        }

    }

    override fun pronadiSveZaKorisnika(korisnik: Korisnik): List<Obavijest>? {
        return try {
            val notificationList = ArrayList<Obavijest>()
            obavijestRepozitorij.findAll().forEach { obavijest ->
                if (!obavijest.procitano && obavijest.korisnik.id == korisnik.id) {
                    notificationList.add(obavijest)
                }
            }
            notificationList
        } catch (e: Exception) {
            logger.error("Exception occur while fetch Notification by User ", e)
            null
        }
    }

    override fun stvoriObavijestObjekt(poruka: String, korisnik: Korisnik): Obavijest {
        var obavijest = Obavijest(poruka, Date(), korisnik)
        obavijestRepozitorij.save(obavijest)
        return obavijest
    }

    override fun pronadiPoKorisnikuIIdu(korisnik: Korisnik, obavijestId: Int?): Obavijest? {
        try {
            return obavijestRepozitorij.findByKorisnikAndId(korisnik, obavijestId)
        } catch (e: Exception) {
            logger.error("Exception occur while fetch Notification by User and Notification Id ", e)
            return null
        }

    }

    override fun pronadiSveProcitanePoKorisniku(korisnik: Korisnik): List<Obavijest>? {
        try {
            val notifications = ArrayList<Obavijest>()
            obavijestRepozitorij.findAll().forEach { obavijest ->
                if (obavijest.procitano && obavijest.korisnik.id === korisnik.id) {
                    notifications.add(obavijest)
                }
            }
            return notifications
        } catch (e: Exception) {
            logger.error("Exception occur while fetching Notification by User ", e)
            return null
        }
    }

    override fun oznaciKaoProcitano(listaObavijesti: List<Obavijest>) {
        for (obavijest in listaObavijesti) {
            obavijest.procitano = true
            obavijestRepozitorij.save(obavijest)
        }
    }
}
