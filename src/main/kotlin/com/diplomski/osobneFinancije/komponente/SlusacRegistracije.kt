package com.diplomski.osobneFinancije.komponente

import com.diplomski.osobneFinancije.dogadaji.OnRegistrationCompleteEvent
import com.diplomski.osobneFinancije.entiteti.Korisnik
import com.diplomski.osobneFinancije.servisi.KorisnikServis
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.ApplicationListener
import org.springframework.context.MessageSource
import org.springframework.core.env.Environment
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.stereotype.Component
import java.util.*


@Component
class SlusacRegistracije : ApplicationListener<OnRegistrationCompleteEvent> {
    @Autowired
    lateinit var service: KorisnikServis

    @Autowired
    @Qualifier("messageSource")
    lateinit var messages: MessageSource

    @Autowired
    lateinit var  mailSender: JavaMailSender

    @Autowired
    lateinit var env: Environment

    override fun onApplicationEvent(event: OnRegistrationCompleteEvent) {
        this.confirmRegistration(event)
    }

    private fun confirmRegistration(event: OnRegistrationCompleteEvent) {
        val user = event.korisnik
        val token = UUID.randomUUID().toString()
        service.stvoriVerifikacisjkiTokenZaKorisnika(user, token)

        val email = constructEmailMessage(event, user, token)
        mailSender.send(email)
    }

    private fun constructEmailMessage(
        event: OnRegistrationCompleteEvent,
        korisnik: Korisnik,
        token: String
    ): SimpleMailMessage {
        val recipientAddress = korisnik.email
        val subject = "Potvrda registracije"
        val confirmationUrl = event.appUrl + "Confirm?token=" + token
        val message = messages.getMessage("message.regSucc", null, event.locale)
        val email = SimpleMailMessage()
        email.setTo(recipientAddress)
        email.setSubject(subject)
        email.setText("$message \r\n$confirmationUrl")
        email.setFrom(env.getProperty("spring.mail.username")!!)
        return email
    }

}