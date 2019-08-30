package com.diplomski.osobneFinancije.komponente

import com.diplomski.osobneFinancije.servisi.KorisnikServis
import com.diplomski.osobneFinancije.servisi.NotificationService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.MessageSource
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import org.springframework.util.ObjectUtils
import java.time.LocalDateTime
import java.util.*

@Component
class SlusacAplikacije {

    @Autowired
    lateinit var service: KorisnikServis

    @Autowired
    lateinit var notificationService: NotificationService

    @Autowired
    @Qualifier("messageSource")
    lateinit var messages: MessageSource

    @Transactional
    @Scheduled(cron = "0 0/1 * * * ?")
    fun publish() {
        val transakcije = service.dohvatiSveTransakcije().stream()
            .filter { entry ->
                !ObjectUtils.isEmpty(entry.danPlacanja) && entry.danPlacanja!!.isBefore(LocalDateTime.now()) && entry.transakcijaProvjerena == false
            }

        for (transakcija in transakcije) {
            transakcija.transakcijaProvjerena = true
            if (!ObjectUtils.isEmpty(transakcija.transakcijaOd) && !ObjectUtils.isEmpty(transakcija.transakcijaPrema)) {
                var korisnikOd = service.findByKorisnickoIme(transakcija.transakcijaOd!!)
                var korisnikPrema = service.findByKorisnickoIme(transakcija.transakcijaPrema!!)
                if (transakcija.naziv!!.contains("Income") && transakcija.danPlacanja!!.isBefore(LocalDateTime.now())) {
                    korisnikOd!!.stanjeRacuna += transakcija.vrijednost
                    korisnikPrema!!.stanjeRacuna -= transakcija.vrijednost
                    notificationService.createNotificationObject(
                        messages.getMessage("label.pay.expense", null, Locale.ENGLISH) + korisnikOd!!.korisnickoIme,
                        korisnikPrema!!
                    )
                    notificationService.createNotificationObject(
                        messages.getMessage("label.pay.income", null, Locale.ENGLISH) + korisnikPrema.korisnickoIme,
                        korisnikOd
                    )
                    service.spremiKorisnika(korisnikOd)
                    service.spremiKorisnika(korisnikPrema)
                } else if (transakcija.naziv!!.contains("Expense") && transakcija.danPlacanja!!.isBefore(LocalDateTime.now())){
                    korisnikOd!!.stanjeRacuna -= transakcija.vrijednost
                    korisnikPrema!!.stanjeRacuna += transakcija.vrijednost
                    notificationService.createNotificationObject(
                        messages.getMessage("label.pay.expense", null, Locale.ENGLISH) + korisnikPrema!!.korisnickoIme,
                        korisnikOd!!
                    )
                    notificationService.createNotificationObject(
                        messages.getMessage("label.pay.income", null, Locale.ENGLISH) + korisnikOd.korisnickoIme,
                        korisnikPrema
                    )
                    service.spremiKorisnika(korisnikOd)
                    service.spremiKorisnika(korisnikPrema)
                }
            }
            service.spremiTransakciju(transakcija)
        }
    }
}