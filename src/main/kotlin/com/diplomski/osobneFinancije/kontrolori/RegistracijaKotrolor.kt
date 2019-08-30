package com.diplomski.osobneFinancije.kontrolori

import com.diplomski.osobneFinancije.dogadaji.OnRegistrationCompleteEvent
import com.diplomski.osobneFinancije.entiteti.Korisnik
import com.diplomski.osobneFinancije.entiteti.Racun
import com.diplomski.osobneFinancije.forme.PasswordDto
import com.diplomski.osobneFinancije.forme.ProfileForm
import com.diplomski.osobneFinancije.forme.RegisterForm
import com.diplomski.osobneFinancije.iznimke.UserNotFoundException
import com.diplomski.osobneFinancije.servisi.impl.KorisnikServisImpl
import com.diplomski.osobneFinancije.utils.GenericResponse
import com.diplomski.osobneFinancije.utils.Konstante.Putanje.OsiguranePutanje.Companion.badUser
import com.diplomski.osobneFinancije.utils.Konstante.Putanje.OsiguranePutanje.Companion.basePath
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
        val registerForm = RegisterForm()
        model.addAttribute("user", registerForm)
        return register
    }

    @RequestMapping(value = [profileDetails], method = [RequestMethod.GET])
    fun showUserDetails(model: Model): String {
        val korisnik = korisnikServis.findByKorisnickoIme(SecurityContextHolder.getContext().authentication.name)
        val profileForm = ProfileForm(korisnik!!)
        model.addAttribute("user", profileForm)
        return displayProfile
    }

    @RequestMapping(value = [userProfile], method = [RequestMethod.GET])
    fun showProfile(model: Model): String {
        val korisnik = korisnikServis.findByKorisnickoIme(SecurityContextHolder.getContext().authentication.name)
        val profileForm = ProfileForm(korisnik!!)
        model.addAttribute("user", profileForm)
        return profile
    }

    @RequestMapping(value = [userProfile], method = [RequestMethod.POST])
    fun updateUserProfile(@ModelAttribute("user") @Valid profileForm: ProfileForm, bindingResult: BindingResult): ModelAndView {
        val korisnik = korisnikServis.findByKorisnickoIme(SecurityContextHolder.getContext().authentication.name)
        profileForm.username = (SecurityContextHolder.getContext().authentication.name)
        profileForm.balance = zbrojiIznosSvihRacuna(korisnik!!.racuni.toList())
        if (bindingResultHasErrors(bindingResult)) {
            return ModelAndView(displayProfile, "user", profileForm)
        } else {
            val user1 = updateUserAccount(korisnik, profileForm)
            korisnikServis.azurirajKorisnika(user1)
            return ModelAndView(displayProfile, "user", profileForm)
        }
    }

    @RequestMapping(value = [registration], method = [RequestMethod.POST])
    fun registerUserAccount(
        @ModelAttribute("user") @Valid userDto: RegisterForm, result: BindingResult,
        request: HttpServletRequest
    ): ModelAndView {
        if (bindingResultHasErrors(result)) {
            return ModelAndView(register, "user", userDto)
        } else {
            val registered = createUserAccount(userDto)
            try {
                val appUrl = request.requestURL.toString()
                eventPublisher.publishEvent(OnRegistrationCompleteEvent(registered, request.locale, appUrl))
            } catch (me: Exception) {
                korisnikServis.obrisiKorisnika(userDto)
                return ModelAndView("error", "user", userDto)
            }

            return ModelAndView(loginPage)
        }
    }

    private fun createUserAccount(accountDto: RegisterForm): Korisnik {
        return korisnikServis.spremiRegistriranogKorisnika(accountDto)
    }

    private fun updateUserAccount(user: Korisnik, profileForm: ProfileForm): Korisnik {
        return korisnikServis.azurirajDetaljeKorisnika(user, profileForm)
    }

    @RequestMapping(value = [resetPassword], method = [RequestMethod.POST])
    fun resetPassword(request: HttpServletRequest, @RequestParam("email") userEmail: String): GenericResponse {
        val korisnik = korisnikServis.findByEmail(userEmail)
        val appUrl = "http://" + request.serverName + ":" + request.serverPort + request.contextPath
        if (ObjectUtils.isEmpty(korisnik)) {
            throw UserNotFoundException()
        }
        val token = UUID.randomUUID().toString()
        korisnikServis.createPasswordResetTokenForUser(korisnik!!, token)
        mailSender.send(constructResetTokenEmail(appUrl, request.locale, token, korisnik))
        return GenericResponse(poruke.getMessage("message.resetPasswordEmail", null, request.locale))
    }

    @RequestMapping(value = [updatePassword], method = [RequestMethod.GET])
    fun showChangePasswordPage(
        passwordDto: PasswordDto,
        locale: Locale, @RequestParam("id") id: Long, @RequestParam("token") token: String
    ): ModelAndView {
        val result = korisnikServis.validirajTokenZaLozinku(id, token)
        if ("valid" != result) {
            val modelAndView = ModelAndView(updatePassword, "user", passwordDto)
            modelAndView.addObject("message", poruke.getMessage("auth.message.$result", null, locale))
            return modelAndView
        }
        return ModelAndView(updatePassword, "user", passwordDto)
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

    @RequestMapping(value = [savePassword], method = [RequestMethod.POST])
    fun savePassword(@ModelAttribute("user") @Valid passwordDto: PasswordDto, result: BindingResult): ModelAndView {
        if (result.hasErrors()) {
            result.globalErrors.forEach { f ->
                if ("PasswordMatches".contains(Objects.requireNonNull(f.code)!!)) {
                    result.rejectValue("lozinka", "message.passError")
                }
            }
            return ModelAndView(updatePassword, "user", passwordDto)
        } else {
            val korisnik = SecurityContextHolder.getContext().authentication.principal as Korisnik
            korisnikServis.promijeniLozinkuKorisniku(korisnik, passwordDto.lozinka!!)
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
