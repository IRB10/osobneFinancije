package com.diplomski.osobneFinancije.kontrolori


import com.diplomski.osobneFinancije.forme.FinancesForm
import com.diplomski.osobneFinancije.repozitoriji.KategorijaRepozitorij
import com.diplomski.osobneFinancije.servisi.KorisnikServis
import com.diplomski.osobneFinancije.utils.Konstante.Putanje.OsiguranePutanje.Companion.addExpense
import com.diplomski.osobneFinancije.utils.Konstante.Putanje.OsiguranePutanje.Companion.addIncome
import com.diplomski.osobneFinancije.utils.Konstante.Putanje.OsiguranePutanje.Companion.addObligations
import com.diplomski.osobneFinancije.utils.Konstante.Putanje.OsiguranePutanje.Companion.displayObligations
import com.diplomski.osobneFinancije.utils.Konstante.Putanje.OsiguranePutanje.Companion.displayPdfReport
import com.diplomski.osobneFinancije.utils.Konstante.Putanje.OsiguranePutanje.Companion.finances
import com.diplomski.osobneFinancije.utils.Konstante.Putanje.OsiguranePutanje.Companion.homepage
import com.diplomski.osobneFinancije.utils.Konstante.Putanje.OsiguranePutanje.Companion.overview
import com.diplomski.osobneFinancije.utils.Konstante.Putanje.OsiguranePutanje.Companion.pathObligations
import com.diplomski.osobneFinancije.utils.Konstante.Putanje.OsiguranePutanje.Companion.pdfReport
import com.diplomski.osobneFinancije.utils.Konstante.Putanje.OsiguranePutanje.Companion.userFinances
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.MessageSource
import org.springframework.core.io.InputStreamResource
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
import org.springframework.web.servlet.ModelAndView
import javax.servlet.http.HttpServletRequest
import javax.validation.Valid

@Controller
class FinancijeKontrolor(
    private val korisnikServis: KorisnikServis,
    private val kategorijaRepozitorij: KategorijaRepozitorij, @param:Qualifier("messageSource") private val messages: MessageSource
) {

    @GetMapping(value = [userFinances])
    fun showFinances(model: Model): String {
        val financesForm = FinancesForm()
        model.addAttribute(finances, financesForm)
        model.addAttribute("kategorije", kategorijaRepozitorij.findAll())
        return finances
    }

    @PostMapping(value = [addIncome])
    fun updatedUserIncome(
        @ModelAttribute("finances") @Valid financesForm: FinancesForm, bindingResult: BindingResult,
        request: HttpServletRequest
    ): ModelAndView {
        val locale = request.locale
        val auth = SecurityContextHolder.getContext().authentication
        val korisnik = korisnikServis.findByKorisnickoIme(auth.name)
        val modelAndView = ModelAndView(finances, finances, financesForm)
        modelAndView.addObject("kategorije", kategorijaRepozitorij.findAll())
        if (bindingResult.hasErrors() || financesForm.income <= 0F) {
            modelAndView.addObject("messageError", messages.getMessage("message.income.failed", null, locale))
        } else {
            modelAndView.addObject("message", messages.getMessage("message.income.added", null, locale))
            korisnikServis.updateIncome(korisnik!!, financesForm.income, financesForm.kategorija!!)
        }
        financesForm.income = 0F
        return modelAndView
    }

    @PostMapping(value = [addExpense])
    fun updateUserProfile(
        @ModelAttribute("finances") @Valid financesForm: FinancesForm, bindingResult: BindingResult,
        request: HttpServletRequest
    ): ModelAndView {
        val locale = request.locale
        val auth = SecurityContextHolder.getContext().authentication
        val korisnik = korisnikServis.findByKorisnickoIme(auth.name)
        val modelAndView = ModelAndView(finances, finances, financesForm)
        modelAndView.addObject("kategorije", kategorijaRepozitorij.findAll())
        if (bindingResult.hasErrors() || financesForm.expense <= 0F) {
            modelAndView.addObject("messageErrorExpense", messages.getMessage("message.expense.failed", null, locale))
        } else {
            modelAndView.addObject("messageExpense", messages.getMessage("message.expense.added", null, locale))
            korisnikServis.updateExpense(korisnik!!, financesForm.expense, financesForm.kategorija!!)
        }
        financesForm.expense = 0F
        return modelAndView
    }

    @GetMapping(value = [homepage])
    fun showUserHomePage(model: Model): String {
        model.addAttribute("obligationList", korisnikServis.dohvatiTransakcijeZaKorisnika())
        return overview
    }

    @GetMapping(value = [pathObligations])
    fun showObligations(model: Model): String {
        val financesForm = FinancesForm()
        model.addAttribute("obligation", financesForm)
        model.addAttribute("kategorije", kategorijaRepozitorij.findAll())
        model.addAttribute("racuni", korisnikServis.dohvatiSveRacune())
        return displayObligations
    }

    @PostMapping(value = [addObligations])
    fun addObligation(
        @ModelAttribute("obligation") @Valid financesForm: FinancesForm, bindingResult: BindingResult,
        request: HttpServletRequest
    ): ModelAndView {
        val locale = request.locale
        val modelAndView = ModelAndView(displayObligations, displayObligations, financesForm)
        modelAndView.addObject("kategorije", kategorijaRepozitorij.findAll())
        modelAndView.addObject("racuni", korisnikServis.dohvatiSveRacune())
        if (bindingResult.hasErrors()) {
            modelAndView.addObject("messageError", messages.getMessage("message.obligation.value.failed", null, locale))
        } else if (!checkFinanceForm(financesForm)) {
            modelAndView.addObject("messageError", messages.getMessage("message.obligation.value.failed", null, locale))
        } else {
            modelAndView.addObject("messageObligation", messages.getMessage("message.obligation.added", null, locale))
            val obligationType = request.getParameter("obligationType")
            val korisnik = financesForm.racunKorisnik
            var financijeKorisnik = 0L
            var financijeRacun = 0L
            if (korisnik != null) {
                financijeRacun = korisnik.split("|")[0].toLong()
                financijeKorisnik = korisnik.split("|")[1].toLong()
            }
            korisnikServis.updateObligation(
                financesForm,
                SecurityContextHolder.getContext().authentication.name,
                obligationType,
                financesForm.kategorija,
                financijeKorisnik,
                financijeRacun,
                locale
            )
        }
        resetFinancesForm(financesForm)
        return modelAndView
    }

    @GetMapping(value = [pdfReport])
    fun displayPdfReportPage(): String {
        return displayPdfReport
    }

    @PostMapping(value = [pdfReport], produces = [MediaType.APPLICATION_PDF_VALUE])
    fun citiesReport(request: HttpServletRequest): ResponseEntity<InputStreamResource> {
        val dateFrom = request.getParameter("dateFrom")
        val dateTo = request.getParameter("dateTo")
        val byteArrayInputStream = korisnikServis.generatePdfForRange(dateFrom, dateTo)
        val headers = HttpHeaders()
        headers.add("Content-Disposition", "inline; filename=obligationReport.pdf")
        return ResponseEntity.ok().headers(headers).contentType(MediaType.APPLICATION_PDF)
            .body(InputStreamResource(byteArrayInputStream))
    }

    private fun checkFinanceForm(financesForm: FinancesForm): Boolean {
        return !ObjectUtils.isEmpty(financesForm) && !ObjectUtils.isEmpty(financesForm.obligationDate) && !ObjectUtils.isEmpty(
            financesForm
                .obligationDetails
        ) && financesForm
            .value != 0f
    }

    private fun resetFinancesForm(financesForm: FinancesForm) {
        financesForm.value = 0F
        financesForm.obligationDate = null
        financesForm.obligationDetails = ""
    }
}