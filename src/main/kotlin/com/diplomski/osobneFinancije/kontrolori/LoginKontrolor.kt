package com.diplomski.osobneFinancije.kontrolori

import com.diplomski.osobneFinancije.utils.Konstante.Putanje.OsnovnePutanje.Companion.forgotPassword
import com.diplomski.osobneFinancije.utils.Konstante.Putanje.OsnovnePutanje.Companion.forgotPasswordPage
import com.diplomski.osobneFinancije.utils.Konstante.Putanje.OsnovnePutanje.Companion.loginPage
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import javax.servlet.http.HttpServletRequest


@Controller
class LoginKontrolor {

    @GetMapping("/login")
    fun login(
        model: Model, request: HttpServletRequest, @RequestParam(
            required = false
        ) error: String?
    ): String {
        var errorPoruka: String?
        if (error != null) {
            errorPoruka = "Kriva kombinacija korisničkog imena i lozinke!"
            model.addAttribute("errorPoruka", errorPoruka)
        }
        return loginPage
    }

    @RequestMapping(value = [forgotPassword], method = [RequestMethod.GET])
    fun showForgotPassword(): String {
        return forgotPasswordPage
    }


}