package com.diplomski.osobneFinancije.kontrolori

import com.diplomski.osobneFinancije.forme.RacunForma
import com.diplomski.osobneFinancije.servisi.KorisnikServis
import com.diplomski.osobneFinancije.utils.Konstante.Putanje.OsiguranePutanje.Companion.account
import com.diplomski.osobneFinancije.utils.Konstante.Putanje.OsiguranePutanje.Companion.accountRoute
import com.diplomski.osobneFinancije.utils.Konstante.Putanje.OsiguranePutanje.Companion.createAccount
import com.diplomski.osobneFinancije.utils.Konstante.Putanje.OsiguranePutanje.Companion.createAccountRoute
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.MessageSource
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.servlet.ModelAndView
import javax.servlet.http.HttpServletRequest
import javax.validation.Valid

@Controller
class RacuniKontrolor(private val korisnikServis: KorisnikServis, @param:Qualifier("messageSource") private val messages: MessageSource) {

    @GetMapping(value = [accountRoute])
    fun showAccount(model: Model): String {
        model.addAttribute("listaRacuna", korisnikServis.dohvatiRacuneKorisnika())
        return account
    }

    @GetMapping(value = [createAccountRoute])
    fun showCreateAccount(model: Model): String {
        val racunForm = RacunForma()
        model.addAttribute("racunForm", racunForm)
        return createAccount
    }

    @PostMapping(value = [createAccountRoute])
    fun createUserAccount(
        @ModelAttribute("racunForm") @Valid racunForma: RacunForma, bindingResult: BindingResult,
        request: HttpServletRequest, model: Model
    ): String {
        val locale = request.locale
        if (bindingResult.hasErrors()) {
            model.addAttribute("messageError", messages.getMessage("message.accountCreation.failed", null, locale))
        } else {
            model.addAttribute("message", messages.getMessage("message.account.added", null, locale))
            korisnikServis.kreirajRacun(racunForma)
        }
        return createAccount
    }
}