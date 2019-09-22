package com.diplomski.osobneFinancije.kontrolori

import com.diplomski.osobneFinancije.servisi.KorisnikServis
import com.diplomski.osobneFinancije.servisi.ObavijestiServis
import com.diplomski.osobneFinancije.utils.Konstante.Putanje.OsiguranePutanje.Companion.newNotifications
import com.diplomski.osobneFinancije.utils.Konstante.Putanje.OsiguranePutanje.Companion.oldNotifications
import org.springframework.data.domain.PageRequest
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import java.util.*
import java.util.stream.Collectors
import java.util.stream.IntStream


@Controller
class ObavijestiKontrolor(
    private val obavijestiServis: ObavijestiServis,
    private val korisnikServis: KorisnikServis
) {

    @RequestMapping(value = [oldNotifications], method = [RequestMethod.GET])
    fun prikaziStareObavijesti(
        model: Model,
        @RequestParam("page") page: Optional<Int>,
        @RequestParam("size") size: Optional<Int>
    ): String {
        val korisnik = korisnikServis.pronadiPoKorisnickomImenu(SecurityContextHolder.getContext().authentication.name)
        val currentPage = page.orElse(1)
        val pageSize = size.orElse(5)

        val listaObavijesti = obavijestiServis.stranicenjeObavijesti(
            PageRequest.of(currentPage - 1, pageSize),
            obavijestiServis.pronadiSveProcitanePoKorisniku(korisnik!!)!!
        )

        model.addAttribute("listaObavijesti", listaObavijesti)

        val totalPages = listaObavijesti.totalPages
        if (totalPages > 0) {
            val pageNumbers = IntStream.rangeClosed(1, totalPages)
                .boxed()
                .collect(Collectors.toList())
            model.addAttribute("pageNumbers", pageNumbers)
        }
        return "notifications"
    }

    @RequestMapping(value = [newNotifications], method = [RequestMethod.GET])
    fun prikaziNoveObavijesti(
        model: Model,
        @RequestParam("page") page: Optional<Int>,
        @RequestParam("size") size: Optional<Int>
    ): String {
        val korisnik = korisnikServis.pronadiPoKorisnickomImenu(SecurityContextHolder.getContext().authentication.name)
        val notificationList = obavijestiServis.pronadiSveZaKorisnika(korisnik!!)
        obavijestiServis.oznaciKaoProcitano(notificationList!!)
        val currentPage = page.orElse(1)
        val pageSize = size.orElse(5)

        val listaObavijesti = obavijestiServis.stranicenjeObavijesti(
            PageRequest.of(currentPage - 1, pageSize),
            notificationList
        )

        model.addAttribute("listaObavijesti", listaObavijesti)

        val totalPages = listaObavijesti.totalPages
        if (totalPages > 0) {
            val pageNumbers = IntStream.rangeClosed(1, totalPages)
                .boxed()
                .collect(Collectors.toList())
            model.addAttribute("pageNumbers", pageNumbers)
        }
        return "notifications"
    }
}