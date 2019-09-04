package com.diplomski.osobneFinancije.komponente

import com.amazonaws.services.lambda.AWSLambdaClientBuilder
import com.amazonaws.services.lambda.invoke.LambdaInvokerFactory
import com.diplomski.osobneFinancije.entiteti.ReportInput
import com.diplomski.osobneFinancije.entiteti.Transakcija
import com.diplomski.osobneFinancije.servisi.FinancijeServis
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
                val korisnikOd = service.findByKorisnickoIme(transakcija.transakcijaOd!!)
                val korisnikPrema = service.findByKorisnickoIme(transakcija.transakcijaPrema!!)
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
                } else if (transakcija.naziv!!.contains("Expense") && transakcija.danPlacanja!!.isBefore(LocalDateTime.now())) {
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

    @Scheduled(cron = "0 0 0 1/1 * ?")
    fun sendReportMail() {
        val financijeServis = LambdaInvokerFactory.builder()
            .lambdaClient(AWSLambdaClientBuilder.defaultClient())
            .build(FinancijeServis::class.java)
        val response =
            financijeServis.lambdaIzvjestaj(ReportInput(stvoriMapuZaAwsLambud()))
        println("Lambda rezultat = ${response.rezultatOutput}")
    }

    fun stvoriMapuZaAwsLambud(): Map<String, Transakcija> {
        val mapaVrijednosti = HashMap<String, Transakcija>()
        for (transakcija in service.dohvatiTransakcijeZaMjesec(LocalDateTime.now().monthValue)) {
            mapaVrijednosti[transakcija.danPlacanja.toString()] = transakcija
        }
        return mapaVrijednosti
    }
}