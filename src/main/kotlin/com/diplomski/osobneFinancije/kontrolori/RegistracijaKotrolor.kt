package com.diplomski.osobneFinancije.kontrolori

import com.diplomski.osobneFinancije.dogadaji.ZavrsenaRegistracijaDogadaj
import com.diplomski.osobneFinancije.entiteti.Korisnik
import com.diplomski.osobneFinancije.entiteti.Racun
import com.diplomski.osobneFinancije.forme.LozinkaDto
import com.diplomski.osobneFinancije.forme.ProfilForma
import com.diplomski.osobneFinancije.forme.RegistracijaForma
import com.diplomski.osobneFinancije.iznimke.KorisnikNijePronadenIznimka
import com.diplomski.osobneFinancije.servisi.impl.KorisnikServisImpl
import com.diplomski.osobneFinancije.utils.Konstante.Putanje.OsiguranePutanje.Companion.badUser
import com.diplomski.osobneFinancije.utils.Konstante.Putanje.OsiguranePutanje.Companion.changePassword
import com.diplomski.osobneFinancije.utils.Konstante.Putanje.OsiguranePutanje.Companion.changePasswordPage
import com.diplomski.osobneFinancije.utils.Konstante.Putanje.OsiguranePutanje.Companion.displayProfile
import com.diplomski.osobneFinancije.utils.Konstante.Putanje.OsiguranePutanje.Companion.profile
import com.diplomski.osobneFinancije.utils.Konstante.Putanje.OsiguranePutanje.Companion.profileDetails
import com.diplomski.osobneFinancije.utils.Konstante.Putanje.OsiguranePutanje.Companion.registerConfirm
import com.diplomski.osobneFinancije.utils.Konstante.Putanje.OsiguranePutanje.Companion.savePassword
import com.diplomski.osobneFinancije.utils.Konstante.Putanje.OsiguranePutanje.Companion.updatePassword
import com.diplomski.osobneFinancije.utils.Konstante.Putanje.OsiguranePutanje.Companion.userProfile
import com.diplomski.osobneFinancije.utils.Konstante.Putanje.OsnovnePutanje.Companion.loginPage
import com.diplomski.osobneFinancije.utils.Konstante.Putanje.OsnovnePutanje.Companion.register
import com.diplomski.osobneFinancije.utils.Konstante.Putanje.OsnovnePutanje.Companion.registration
import com.diplomski.osobneFinancije.utils.Konstante.Putanje.OsnovnePutanje.Companion.resetPassword
import com.diplomski.osobneFinancije.utils.StandardniOdgovor
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.MessageSource
import org.springframework.core.env.Environment
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.util.ObjectUtils
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.ModelAndView
import java.util.*
import javax.servlet.http.HttpServletRequest
import javax.validation.Valid


@Controller
class RegistracijaKotrolor(
    private val korisnikServis: KorisnikServisImpl,
    @param:Qualifier("messageSource") private val poruke: MessageSource,
    private val mailSender: JavaMailSender,
    private val env: Environment,
    private val eventPublisher: ApplicationEventPublisher
) {

    @RequestMapping(value = [registration], method = [RequestMethod.GET])
    fun showRegistrationForm(model: Model): String {
        val registerForm = RegistracijaForma()
        model.addAttribute("user", registerForm)
        return register
    }

    @RequestMapping(value = [profileDetails], method = [RequestMethod.GET])
    fun showUserDetails(model: Model): String {
        val korisnik = korisnikServis.pronadiPoKorisnickomImenu(SecurityContextHolder.getContext().authentication.name)
        val profileForm = ProfilForma(korisnik!!)
        model.addAttribute("user", profileForm)
        return displayProfile
    }

    @RequestMapping(value = [userProfile], method = [RequestMethod.GET])
    fun showProfile(model: Model): String {
        val korisnik = korisnikServis.pronadiPoKorisnickomImenu(SecurityContextHolder.getContext().authentication.name)
        val profileForm = ProfilForma(korisnik!!)
        model.addAttribute("user", profileForm)
        return profile
    }

    @RequestMapping(value = [userProfile], method = [RequestMethod.POST])
    fun updateUserProfile(@ModelAttribute("user") @Valid profilForma: ProfilForma, bindingResult: BindingResult): ModelAndView {
        val korisnik = korisnikServis.pronadiPoKorisnickomImenu(SecurityContextHolder.getContext().authentication.name)
        profilForma.korisnickoIme = (SecurityContextHolder.getContext().authentication.name)
        profilForma.stanjeRacuna = korisnik!!.stanjeRacuna + zbrojiIznosSvihRacuna(korisnik.racuni.toList())
        if (bindingResultHasErrors(bindingResult)) {
            return ModelAndView(displayProfile, "user", profilForma)
        } else {
            val user1 = updateUserAccount(korisnik, profilForma)
            korisnikServis.azurirajKorisnika(user1)
            return ModelAndView(displayProfile, "user", profilForma)
        }
    }

    @RequestMapping(value = [registration], method = [RequestMethod.POST])
    fun registerUserAccount(
        @ModelAttribute("user") @Valid userDto: RegistracijaForma, result: BindingResult,
        request: HttpServletRequest
    ): ModelAndView {
        if (bindingResultHasErrors(result)) {
            return ModelAndView(register, "user", userDto)
        } else {
            val registered = createUserAccount(userDto)
            try {
                val appUrl = request.requestURL.toString()
                eventPublisher.publishEvent(ZavrsenaRegistracijaDogadaj(registered, request.locale, appUrl))
            } catch (me: Exception) {
                korisnikServis.obrisiKorisnika(userDto)
                return ModelAndView("error", "user", userDto)
            }

            return ModelAndView(loginPage)
        }
    }

    private fun createUserAccount(accountDto: RegistracijaForma): Korisnik {
        return korisnikServis.spremiRegistriranogKorisnika(accountDto)
    }

    private fun updateUserAccount(user: Korisnik, profilForma: ProfilForma): Korisnik {
        return korisnikServis.azurirajDetaljeKorisnika(user, profilForma)
    }

    @RequestMapping(value = [resetPassword], method = [RequestMethod.POST])
    fun resetPassword(request: HttpServletRequest, @RequestParam("email") userEmail: String): StandardniOdgovor {
        val korisnik = korisnikServis.pronadiPoEmailu(userEmail)
        val appUrl = "http://" + request.serverName + ":" + request.serverPort + request.contextPath
        if (ObjectUtils.isEmpty(korisnik)) {
            throw KorisnikNijePronadenIznimka()
        }
        val token = UUID.randomUUID().toString()
        korisnikServis.kreirajTokenZaObnovuLozinkeKorisniku(korisnik!!, token)
        mailSender.send(constructResetTokenEmail(appUrl, request.locale, token, korisnik))
        return StandardniOdgovor(poruke.getMessage("message.resetPasswordEmail", null, request.locale))
    }

    @RequestMapping(value = [updatePassword], method = [RequestMethod.GET])
    fun showChangePasswordPage(
        lozinkaDto: LozinkaDto,
        locale: Locale, @RequestParam("id") id: Long, @RequestParam("token") token: String
    ): ModelAndView {
        val result = korisnikServis.validirajTokenZaLozinku(id, token)
        if ("valid" != result) {
            val modelAndView = ModelAndView(updatePassword, "user", lozinkaDto)
            modelAndView.addObject("message", poruke.getMessage("auth.message.$result", null, locale))
            return modelAndView
        }
        return ModelAndView(updatePassword, "user", lozinkaDto)
    }

    @RequestMapping(value = [registerConfirm], method = [RequestMethod.GET])
    fun confirmRegistration(request: WebRequest, model: Model, @RequestParam("token") token: String): ModelAndView {
        val locale = request.locale
        val modelAndView = ModelAndView(badUser, "user", model)
        if ("invalid" == korisnikServis.validirajRegistracijskiToken(token)) {
            return modelAndView.addObject("message", poruke.getMessage("auth.message.invalidToken", null, locale))
        }
        if ("expired" == korisnikServis.validirajRegistracijskiToken(token)) {
            return modelAndView.addObject("message", poruke.getMessage("auth.message.expired", null, locale))
        }
        var korisnik = korisnikServis.dohvatiVerifikacijskiToken(token).korisnik
        korisnik!!.aktivan = true
        korisnikServis.spremiKorisnika(korisnik)
        return ModelAndView(loginPage)
    }

    @RequestMapping(value = [changePassword], method = [RequestMethod.GET])
    fun showProfileChangePassword(): ModelAndView {
        return ModelAndView(changePasswordPage, "user", LozinkaDto())
    }

    @RequestMapping(value = [changePassword], method = [RequestMethod.POST])
    fun profileChangePassword(@ModelAttribute("user") @Valid lozinkaDto: LozinkaDto, result: BindingResult): ModelAndView {
        if (!korisnikServis.staraLozinkuKorisnikaValidna(
                SecurityContextHolder.getContext().authentication.name,
                lozinkaDto.staraLozinka
            )
        ) {
            result.rejectValue("staraLozinka", "messages.old.password.incorrect")
            return ModelAndView(changePasswordPage, "user", lozinkaDto)
        }
        return if (result.hasErrors()) {
            result.globalErrors.forEach { f ->
                if ("PasswordMatches".contains(Objects.requireNonNull(f.code)!!)) {
                    result.rejectValue("lozinka", "message.passError")
                }
            }
            ModelAndView(changePasswordPage, "user", lozinkaDto)
        } else {
            val korisnik =
                korisnikServis.pronadiPoKorisnickomImenu(SecurityContextHolder.getContext().authentication.name)
            if (korisnik != null) {
                korisnikServis.promijeniLozinkuKorisniku(korisnik, lozinkaDto.lozinka!!)
            }
            SecurityContextHolder.clearContext()
            ModelAndView(loginPage)
        }
    }

    @RequestMapping(value = [savePassword], method = [RequestMethod.POST])
    fun savePassword(@ModelAttribute("user") @Valid lozinkaDto: LozinkaDto, result: BindingResult): ModelAndView {
        if (result.hasErrors()) {
            result.globalErrors.forEach { f ->
                if ("PasswordMatches".contains(Objects.requireNonNull(f.code)!!)) {
                    result.rejectValue("lozinka", "message.passError")
                }
            }
            return ModelAndView(updatePassword, "user", lozinkaDto)
        } else {
            val korisnik = SecurityContextHolder.getContext().authentication.principal as Korisnik
            korisnikServis.promijeniLozinkuKorisniku(korisnik, lozinkaDto.lozinka!!)
            SecurityContextHolder.clearContext()
            return ModelAndView(loginPage)
        }
    }

    private fun constructEmail(body: String, korisnik: Korisnik): SimpleMailMessage {
        val email = SimpleMailMessage()
        email.setSubject("Reset Password")
        email.setText(body)
        email.setTo(korisnik.email)
        email.setFrom(env.getProperty("spring.mail.username")!!)
        return email
    }

    private fun constructResetTokenEmail(
        contextPath: String,
        locale: Locale,
        token: String,
        korisnik: Korisnik
    ): SimpleMailMessage {
        val url = contextPath + "/updatePassword?id=" + korisnik.id + "&token=" + token
        val message = poruke.getMessage("message.resetPassword", null, locale)
        return constructEmail("$message \r\n$url", korisnik)
    }

    private fun bindingResultHasErrors(bindingResult: BindingResult): Boolean {
        if (bindingResult.hasErrors()) {
            bindingResult.globalErrors.forEach { f ->
                if ("PasswordMatches".contains(Objects.requireNonNull(f.code)!!)) {
                    bindingResult.rejectValue("lozinka", "message.passError")
                }
            }
            return true
        }
        return false
    }

    fun zbrojiIznosSvihRacuna(racuni: List<Racun>): Double {
        var ukupno = 0.0
        for (racun in racuni) {
            ukupno += racun.iznos
        }
        return ukupno
    }
}
