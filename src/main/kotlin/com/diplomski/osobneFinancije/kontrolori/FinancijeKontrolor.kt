package com.diplomski.osobneFinancije.kontrolori


import com.diplomski.osobneFinancije.forme.FinancijeForma
import com.diplomski.osobneFinancije.forme.KategorijaForma
import com.diplomski.osobneFinancije.repozitoriji.KategorijaRepozitorij
import com.diplomski.osobneFinancije.servisi.KorisnikServis
import com.diplomski.osobneFinancije.utils.Konstante.Putanje.OsiguranePutanje.Companion.addExpense
import com.diplomski.osobneFinancije.utils.Konstante.Putanje.OsiguranePutanje.Companion.addIncome
import com.diplomski.osobneFinancije.utils.Konstante.Putanje.OsiguranePutanje.Companion.addObligations
import com.diplomski.osobneFinancije.utils.Konstante.Putanje.OsiguranePutanje.Companion.categories
import com.diplomski.osobneFinancije.utils.Konstante.Putanje.OsiguranePutanje.Companion.displayObligations
import com.diplomski.osobneFinancije.utils.Konstante.Putanje.OsiguranePutanje.Companion.displayPdfReport
import com.diplomski.osobneFinancije.utils.Konstante.Putanje.OsiguranePutanje.Companion.finances
import com.diplomski.osobneFinancije.utils.Konstante.Putanje.OsiguranePutanje.Companion.homepage
import com.diplomski.osobneFinancije.utils.Konstante.Putanje.OsiguranePutanje.Companion.overview
import com.diplomski.osobneFinancije.utils.Konstante.Putanje.OsiguranePutanje.Companion.pathObligations
import com.diplomski.osobneFinancije.utils.Konstante.Putanje.OsiguranePutanje.Companion.pdfReport
import com.diplomski.osobneFinancije.utils.Konstante.Putanje.OsiguranePutanje.Companion.userCategories
import com.diplomski.osobneFinancije.utils.Konstante.Putanje.OsiguranePutanje.Companion.userFinances
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.MessageSource
import org.springframework.core.io.InputStreamResource
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.util.ObjectUtils
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.servlet.ModelAndView
import java.sql.Timestamp
import java.time.LocalDateTime
import java.util.*
import java.util.stream.Collectors
import java.util.stream.IntStream
import javax.servlet.http.HttpServletRequest
import javax.validation.Valid

@Controller
class FinancijeKontrolor(
    private val korisnikServis: KorisnikServis,
    private val kategorijaRepozitorij: KategorijaRepozitorij, @param:Qualifier("messageSource") private val messages: MessageSource
) {

    @GetMapping(value = [userFinances])
    fun showFinances(model: Model): String {
        val financesForm = FinancijeForma()
        model.addAttribute(finances, financesForm)
        model.addAttribute("kategorije", kategorijaRepozitorij.findAll())
        return finances
    }

    @GetMapping(value = [userCategories])
    fun showCategories(model: Model): ModelAndView {
        return ModelAndView(categories, "kategorija", KategorijaForma())
    }

    @PostMapping(value = [userCategories])
    fun addCategories(
        @ModelAttribute("kategorija") @Valid kategorijaForma: KategorijaForma, bindingResult: BindingResult,
        request: HttpServletRequest
    ): ModelAndView {
        val locale = request.locale
        val modelAndView = ModelAndView(categories, "kategorija", kategorijaForma)
        if (bindingResult.hasErrors()) {
            modelAndView.addObject("messageError", messages.getMessage("message.category.failed", null, locale))
        } else {
            modelAndView.addObject("message", messages.getMessage("message.category.succeded", null, locale))
            korisnikServis.dodajKategoriju(kategorijaForma)
        }
        return modelAndView
    }

    @PostMapping(value = [addIncome])
    fun updatedUserIncome(
        @ModelAttribute("finances") @Valid financijeForma: FinancijeForma, bindingResult: BindingResult,
        request: HttpServletRequest
    ): ModelAndView {
        val locale = request.locale
        val auth = SecurityContextHolder.getContext().authentication
        val korisnik = korisnikServis.pronadiPoKorisnickomImenu(auth.name)
        val modelAndView = ModelAndView(finances, finances, financijeForma)
        modelAndView.addObject("kategorije", kategorijaRepozitorij.findAll())
        if (bindingResult.hasErrors() || financijeForma.prihod <= 0F) {
            modelAndView.addObject("messageError", messages.getMessage("message.income.failed", null, locale))
        } else {
            modelAndView.addObject("message", messages.getMessage("message.income.added", null, locale))
            korisnikServis.azurirajPrihod(korisnik!!, financijeForma.prihod, financijeForma.kategorija!!)
        }
        financijeForma.prihod = 0F
        return modelAndView
    }

    @PostMapping(value = [addExpense])
    fun updateUserProfile(
        @ModelAttribute("finances") @Valid financijeForma: FinancijeForma, bindingResult: BindingResult,
        request: HttpServletRequest
    ): ModelAndView {
        val locale = request.locale
        val auth = SecurityContextHolder.getContext().authentication
        val korisnik = korisnikServis.pronadiPoKorisnickomImenu(auth.name)
        val modelAndView = ModelAndView(finances, finances, financijeForma)
        modelAndView.addObject("kategorije", kategorijaRepozitorij.findAll())
        if (bindingResult.hasErrors() || financijeForma.troskovi <= 0F) {
            modelAndView.addObject("messageErrorExpense", messages.getMessage("message.expense.failed", null, locale))
        } else {
            modelAndView.addObject("messageExpense", messages.getMessage("message.expense.added", null, locale))
            korisnikServis.azurirajTrosak(korisnik!!, financijeForma.troskovi, financijeForma.kategorija!!)
        }
        financijeForma.troskovi = 0F
        return modelAndView
    }

    @GetMapping(value = [homepage])
    fun showUserHomePage(
        model: Model,
        @RequestParam("page") page: Optional<Int>,
        @RequestParam("size") size: Optional<Int>
    ): String {
        val korisnik = korisnikServis.pronadiPoKorisnickomImenu(SecurityContextHolder.getContext().authentication.name)
        korisnik!!.datum_prijave = Timestamp.valueOf(LocalDateTime.now())
        korisnikServis.spremiKorisnika(korisnik)
        val currentPage = page.orElse(1)
        val pageSize = size.orElse(5)

        val listaObveza = korisnikServis.stranicenjeTransakcija(PageRequest.of(currentPage - 1, pageSize), korisnikServis.dohvatiTransakcijeZaKorisnika())

        model.addAttribute("listaObveza", listaObveza)

        val totalPages = listaObveza.totalPages
        if (totalPages > 0) {
            val pageNumbers = IntStream.rangeClosed(1, totalPages)
                .boxed()
                .collect(Collectors.toList())
            model.addAttribute("pageNumbers", pageNumbers)
        }
        return overview
    }

    @GetMapping(value = [pathObligations])
    fun showObligations(model: Model): String {
        val financesForm = FinancijeForma()
        model.addAttribute("obligation", financesForm)
        model.addAttribute("kategorije", kategorijaRepozitorij.findAll())
        model.addAttribute("racuni", korisnikServis.dohvatiSveRacuneDostupneKorisniku())
        return displayObligations
    }

    @PostMapping(value = [addObligations])
    fun addObligation(
        @ModelAttribute("obligation") @Valid financijeForma: FinancijeForma, bindingResult: BindingResult,
        request: HttpServletRequest
    ): ModelAndView {
        val locale = request.locale
        val modelAndView = ModelAndView(displayObligations, displayObligations, financijeForma)
        modelAndView.addObject("kategorije", kategorijaRepozitorij.findAll())
        modelAndView.addObject("racuni", korisnikServis.dohvatiSveRacuneDostupneKorisniku())
        if (bindingResult.hasErrors()) {
            modelAndView.addObject("messageError", messages.getMessage("message.obligation.value.failed", null, locale))
        } else if (!checkFinanceForm(financijeForma)) {
            modelAndView.addObject("messageError", messages.getMessage("message.obligation.value.failed", null, locale))
        } else {
            modelAndView.addObject("messageObligation", messages.getMessage("message.obligation.added", null, locale))
            val obligationType = request.getParameter("obligationType")
            val korisnik = financijeForma.racunKorisnik
            var financijeKorisnik = 0L
            var financijeRacun = 0L
            if (korisnik != null) {
                financijeRacun = korisnik.split("|")[0].toLong()
                financijeKorisnik = korisnik.split("|")[1].toLong()
            }
            korisnikServis.azurirajTransakciju(
                financijeForma,
                SecurityContextHolder.getContext().authentication.name,
                obligationType,
                financijeForma.kategorija,
                financijeKorisnik,
                financijeRacun,
                locale
            )
        }
        resetFinancesForm(financijeForma)
        return modelAndView
    }

    @GetMapping(value = [pdfReport])
    fun displayPdfReportPage(): String {
        return displayPdfReport
    }

    @PostMapping(value = [pdfReport], produces = [MediaType.APPLICATION_PDF_VALUE])
    fun citiesReport(request: HttpServletRequest): ResponseEntity<InputStreamResource> {
        val datumOd = request.getParameter("dateFrom")
        val datumDo = request.getParameter("dateTo")
        val byteArrayInputStream = korisnikServis.generirajPdfZaRasponDatuma(datumOd, datumDo, request.locale)
        val headers = HttpHeaders()
        headers.add("Content-Disposition", "inline; filename=obligationReport.pdf")
        return ResponseEntity.ok().headers(headers).contentType(MediaType.APPLICATION_PDF)
            .body(InputStreamResource(byteArrayInputStream))
    }

    private fun checkFinanceForm(financijeForma: FinancijeForma): Boolean {
        return !ObjectUtils.isEmpty(financijeForma) && !ObjectUtils.isEmpty(financijeForma.datumObveze) && !ObjectUtils.isEmpty(
            financijeForma
                .detaljiObveze
        ) && financijeForma
            .vrijednost != 0f
    }

    private fun resetFinancesForm(financijeForma: FinancijeForma) {
        financijeForma.vrijednost = 0F
        financijeForma.datumObveze = null
        financijeForma.detaljiObveze = ""
    }
}