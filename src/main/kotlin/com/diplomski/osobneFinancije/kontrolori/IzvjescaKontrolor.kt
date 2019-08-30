package com.diplomski.osobneFinancije.kontrolori

import com.diplomski.osobneFinancije.servisi.KorisnikServis
import com.diplomski.osobneFinancije.utils.Konstante.Putanje.OsiguranePutanje.Companion.displayMonthReport
import com.diplomski.osobneFinancije.utils.Konstante.Putanje.OsiguranePutanje.Companion.displayYearReport
import com.diplomski.osobneFinancije.utils.Konstante.Putanje.OsiguranePutanje.Companion.monthReport
import com.diplomski.osobneFinancije.utils.Konstante.Putanje.OsiguranePutanje.Companion.yearReport
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import java.time.LocalDateTime
import javax.servlet.http.HttpServletRequest


@Controller
class IzvjescaKontrolor(private val korisnikServis: KorisnikServis) {

    @GetMapping(value = [monthReport])
    fun displayCurrentMonthReport(model: Model): String {
        model.addAttribute("obligationList", korisnikServis.dohvatiTransakcijeZaMjesec(LocalDateTime.now().monthValue))
        return displayMonthReport
    }

    @PostMapping(value = [monthReport])
    fun showFinances(model: Model, request: HttpServletRequest): String {
        model.addAttribute(
            "obligationList",
            korisnikServis.dohvatiTransakcijeZaMjesec(Integer.parseInt(request.getParameter("month")))
        )
        return displayMonthReport
    }

    @GetMapping(value = [yearReport])
    fun displayCurrentYearReport(model: Model): String {
        model.addAttribute("obligationList", korisnikServis.dohvatiTransakcijeZaGodinu(LocalDateTime.now().year))
        return displayYearReport
    }
}