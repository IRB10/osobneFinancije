package com.diplomski.osobneFinancije.komponente

import com.amazonaws.services.lambda.AWSLambdaClientBuilder
import com.amazonaws.services.lambda.invoke.LambdaInvokerFactory
import com.diplomski.osobneFinancije.entiteti.Korisnik
import com.diplomski.osobneFinancije.entiteti.ReportInput
import com.diplomski.osobneFinancije.entiteti.Transakcija
import com.diplomski.osobneFinancije.servisi.FinancijeServis
import com.diplomski.osobneFinancije.servisi.KorisnikServis
import com.diplomski.osobneFinancije.servisi.ObavijestiServis
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.MessageSource
import org.springframework.core.env.Environment
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import org.springframework.util.ObjectUtils
import java.time.LocalDateTime
import java.util.*
import kotlin.streams.toList

@Component
class SlusacAplikacije {

    @Autowired
    lateinit var korisnikServis: KorisnikServis

    @Autowired
    lateinit var obavijestiServis: ObavijestiServis

    @Autowired
    lateinit var mailSender: JavaMailSender

    @Autowired
    lateinit var environment: Environment

    @Autowired
    @Qualifier("messageSource")
    lateinit var messages: MessageSource

    @Transactional
    @Scheduled(cron = "0 0/1 * * * ?")
    fun publish() {
        val transakcije = korisnikServis.dohvatiSveTransakcije().stream()
            .filter { entry ->
                !ObjectUtils.isEmpty(entry.danPlacanja) && entry.danPlacanja!!.isBefore(LocalDateTime.now()) && entry.transakcijaProvjerena == false
            }

        for (transakcija in transakcije) {
            transakcija.transakcijaProvjerena = true
            if (!ObjectUtils.isEmpty(transakcija.transakcijaOd) && !ObjectUtils.isEmpty(transakcija.transakcijaPrema)) {
                val korisnikOd = korisnikServis.pronadiPoKorisnickomImenu(transakcija.transakcijaOd!!)
                val korisnikPrema = korisnikServis.pronadiPoKorisnickomImenu(transakcija.transakcijaPrema!!)
                if (transakcija.naziv!!.contains("Income") && transakcija.danPlacanja!!.isBefore(LocalDateTime.now())) {
                    korisnikOd!!.stanjeRacuna += transakcija.vrijednost
                    korisnikPrema!!.stanjeRacuna -= transakcija.vrijednost
                    obavijestiServis.stvoriObavijestObjekt(
                        messages.getMessage("label.pay.expense", null, Locale.ENGLISH) + korisnikOd!!.korisnickoIme,
                        korisnikPrema!!
                    )
                    obavijestiServis.stvoriObavijestObjekt(
                        messages.getMessage("label.pay.income", null, Locale.ENGLISH) + korisnikPrema.korisnickoIme,
                        korisnikOd
                    )
                    korisnikServis.spremiKorisnika(korisnikOd)
                    korisnikServis.spremiKorisnika(korisnikPrema)
                } else if (transakcija.naziv!!.contains("Expense") && transakcija.danPlacanja!!.isBefore(LocalDateTime.now())) {
                    korisnikOd!!.stanjeRacuna -= transakcija.vrijednost
                    korisnikPrema!!.stanjeRacuna += transakcija.vrijednost
                    obavijestiServis.stvoriObavijestObjekt(
                        messages.getMessage("label.pay.expense", null, Locale.ENGLISH) + korisnikPrema!!.korisnickoIme,
                        korisnikOd!!
                    )
                    obavijestiServis.stvoriObavijestObjekt(
                        messages.getMessage("label.pay.income", null, Locale.ENGLISH) + korisnikOd.korisnickoIme,
                        korisnikPrema
                    )
                    korisnikServis.spremiKorisnika(korisnikOd)
                    korisnikServis.spremiKorisnika(korisnikPrema)
                }
            }
            korisnikServis.spremiTransakciju(transakcija)
        }
    }

    //@Scheduled(cron = "0 0/1 * * * ?")
    @Scheduled(cron = "0 0 12 * * ?")
    fun reminderMail() {
        for (korisnik in korisnikServis.dohvatiSveKorisnike()) {
            val transakcije = korisnikServis.dohvatiTransakcijeZaKorisnika(korisnik.korisnickoIme).stream()
                .filter { entry ->
                    !ObjectUtils.isEmpty(entry.danPlacanja) && entry.danPlacanja!!.isAfter(LocalDateTime.now())
                }
            var formatiranaPoruka: String = ""
            for (transakcija in transakcije.toList()) {
                if (transakcija.danPlacanja!!.toLocalDate() == LocalDateTime.now().plusDays(1).toLocalDate()) {
                    formatiranaPoruka += "Naziv: " + transakcija.naziv + "\n"
                    formatiranaPoruka += "Vrijednost: " + transakcija.vrijednost + "\n"
                    formatiranaPoruka += "Dan plaćanja: " + transakcija.danPlacanja + "\n"
                    formatiranaPoruka += "Kategorija: " + transakcija.kategorija_id!!.naziv + "\n"
                }
            }
            if (formatiranaPoruka != "") {
                mailSender.send(constructEmailMessage(korisnik, formatiranaPoruka))
            }
        }

    }

    private fun constructEmailMessage(
        korisnik: Korisnik,
        formatiranaPoruka: String
    ): SimpleMailMessage {
        val recipientAddress = korisnik.email
        val subject = messages.getMessage("messages.reminder.obligations", null, Locale.US)
        val message = messages.getMessage("message.obligation.tomorrow", null, Locale.US)
        val email = SimpleMailMessage()
        email.setTo(recipientAddress)
        email.setSubject(subject)
        email.setText("$message \r\n\n$formatiranaPoruka")
        email.setFrom(environment.getProperty("spring.mail.username")!!)
        return email
    }

    //@Scheduled(cron = "0 0/1 * * * ?")
    @Scheduled(cron = "0 0 0 1/1 * ?")
    fun sendReportMail() {
        val financijeServis = LambdaInvokerFactory.builder()
            .lambdaClient(AWSLambdaClientBuilder.defaultClient())
            .build(FinancijeServis::class.java)
        val response =
            financijeServis.lambdaIzvjestaj(ReportInput(stvoriMapuZaAwsLambdu()))
        println("Lambda rezultat = ${response.rezultatOutput}")
    }

    fun stvoriMapuZaAwsLambdu(): Map<String, Transakcija> {
        val mapaVrijednosti = HashMap<String, Transakcija>()
        for (transakcija in korisnikServis.dohvatiSveTransakcijeZaDan()) {
            mapaVrijednosti[transakcija.danPlacanja.toString()] = transakcija
        }
        return mapaVrijednosti
    }
}