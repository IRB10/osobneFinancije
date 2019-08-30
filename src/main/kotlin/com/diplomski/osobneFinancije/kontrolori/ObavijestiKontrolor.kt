package com.diplomski.osobneFinancije.kontrolori

import com.diplomski.osobneFinancije.servisi.KorisnikServis
import com.diplomski.osobneFinancije.servisi.NotificationService
import com.diplomski.osobneFinancije.utils.Konstante.Putanje.OsiguranePutanje.Companion.newNotifications
import com.diplomski.osobneFinancije.utils.Konstante.Putanje.OsiguranePutanje.Companion.oldNotifications
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod


@Controller
class ObavijestiKontrolor(
    private val notificationService: NotificationService,
    private val korisnikServis: KorisnikServis
) {

    @RequestMapping(value = [oldNotifications], method = [RequestMethod.GET])
    fun prikaziStareObavijesti(model: Model): String {
        val korisnik = korisnikServis.findByKorisnickoIme(SecurityContextHolder.getContext().authentication.name)
        val notificationList = notificationService.findAllReadByUser(korisnik!!)
        model.addAttribute("notificationList", notificationList)
        return "notifications"
    }

    @RequestMapping(value = [newNotifications], method = [RequestMethod.GET])
    fun prikaziNoveObavijesti(model: Model): String {
        val korisnik = korisnikServis.findByKorisnickoIme(SecurityContextHolder.getContext().authentication.name)
        val notificationList = notificationService.findAllByUser(korisnik!!)
        notificationService.setAsRead(notificationList!!)
        model.addAttribute("notificationList", notificationList)
        return "notifications"
    }
}