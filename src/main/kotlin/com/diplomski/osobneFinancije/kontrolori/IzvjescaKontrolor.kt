package com.diplomski.osobneFinancije.kontrolori

import com.diplomski.osobneFinancije.servisi.KorisnikServis
import com.diplomski.osobneFinancije.utils.Konstante.Putanje.OsiguranePutanje.Companion.displayMonthReport
import com.diplomski.osobneFinancije.utils.Konstante.Putanje.OsiguranePutanje.Companion.displayYearReport
import com.diplomski.osobneFinancije.utils.Konstante.Putanje.OsiguranePutanje.Companion.monthReport
import com.diplomski.osobneFinancije.utils.Konstante.Putanje.OsiguranePutanje.Companion.yearReport
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import java.time.LocalDateTime
import java.util.*
import java.util.stream.Collectors
import java.util.stream.IntStream
import kotlin.streams.toList


@Controller
class IzvjescaKontrolor(private val korisnikServis: KorisnikServis) {

    @GetMapping(value = [monthReport])
    fun displayCurrentMonthReport(
        model: Model,
        @RequestParam("page") page: Optional<Int>,
        @RequestParam("size") size: Optional<Int>,
        @RequestParam("month") month: Optional<Int>
    ): String {
        val currentPage = page.orElse(1)
        val pageSize = size.orElse(5)
        val monthCurrent = month.orElse(LocalDateTime.now().monthValue)

        val listaObveza = korisnikServis.stranicenjeTransakcija(
            PageRequest.of(currentPage - 1, pageSize),
            korisnikServis.dohvatiTransakcijeZaMjesec(monthCurrent)
        )
        model.addAttribute("listaObveza", listaObveza)
        model.addAttribute("monthCurrent",monthCurrent)
        val totalPages = listaObveza.totalPages
        if (totalPages > 0) {
            val pageNumbers = IntStream.rangeClosed(1, totalPages)
                .boxed()
                .collect(Collectors.toList())
            model.addAttribute("pageNumbers", pageNumbers)
        }
        return displayMonthReport
    }

    @GetMapping(value = [yearReport])
    fun displayCurrentYearReport(
        model: Model,
        @RequestParam("page") page: Optional<Int>,
        @RequestParam("size") size: Optional<Int>
    ): String {
        val currentPage = page.orElse(1)
        val pageSize = size.orElse(5)

        val listaObveza = korisnikServis.stranicenjeTransakcija(
            PageRequest.of(currentPage - 1, pageSize),
            korisnikServis.dohvatiSveTransakcijeReaktivnoZaGodinu(LocalDateTime.now().year).toStream().toList()
        )

        model.addAttribute("listaObveza", listaObveza)

        val totalPages = listaObveza.totalPages
        if (totalPages > 0) {
            val pageNumbers = IntStream.rangeClosed(1, totalPages)
                .boxed()
                .collect(Collectors.toList())
            model.addAttribute("pageNumbers", pageNumbers)
        }
        return displayYearReport
    }
}