package com.diplomski.osobneFinancije.kontrolori

import com.diplomski.osobneFinancije.repozitoriji.KategorijaRepozitorij
import com.diplomski.osobneFinancije.servisi.KorisnikServis
import com.diplomski.osobneFinancije.utils.ExcelView
import com.diplomski.osobneFinancije.utils.Konstante.Putanje.OsiguranePutanje.Companion.csvData
import com.diplomski.osobneFinancije.utils.Konstante.Putanje.OsiguranePutanje.Companion.csvDataImport
import com.diplomski.osobneFinancije.utils.Konstante.Putanje.OsiguranePutanje.Companion.displayEntries
import com.diplomski.osobneFinancije.utils.Konstante.Putanje.OsiguranePutanje.Companion.overview
import org.apache.poi.EmptyFileException
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.openxml4j.exceptions.InvalidFormatException
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.MessageSource
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.servlet.ModelAndView
import java.io.IOException
import java.util.*
import java.util.stream.Collectors
import java.util.stream.IntStream
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Controller
class PodaciKontrolor(
    private val korisnikServis: KorisnikServis, @param:Qualifier("messageSource") private val messages: MessageSource,
    private val kategorijaRepozitorij: KategorijaRepozitorij
) {

    @PostMapping(value = [csvData])
    @Throws(IOException::class)
    fun exportEntries(response: HttpServletResponse, request: HttpServletRequest) {
        val excelView = ExcelView(messages, kategorijaRepozitorij, korisnikServis)
        excelView.generateExcel(
            response,
            HSSFWorkbook(),
            korisnikServis.dohvatiTransakcijeZaKorisnika(),
            request.locale
        )
    }

    @PostMapping(value = [csvDataImport])
    fun importEntries(
        @RequestParam("file") excelDataFile: MultipartFile, model: Model,
        request: HttpServletRequest,
        @RequestParam("page") page: Optional<Int>,
        @RequestParam("size") size: Optional<Int>
    ): String {
        val locale = request.locale
        val excelView = ExcelView(messages, kategorijaRepozitorij, korisnikServis)
        try {
            val entryList = excelView.importExcel(excelDataFile)
            if (korisnikServis.spremiUvezeneTransakcije(entryList) == 0) {
                model.addAttribute("errorImport", messages.getMessage("message.zero.import", null, locale))
            } else {
                model.addAttribute("notificationImport", messages.getMessage("message.numbers.import", null, locale))
            }
        } catch (e: IOException) {
            model.addAttribute("errorImport", messages.getMessage("message.error.import", null, locale))
        } catch (e: EmptyFileException) {
            model.addAttribute("errorImport", messages.getMessage("message.error.import", null, locale))
        } catch (e: InvalidFormatException) {
            model.addAttribute("errorImport", messages.getMessage("message.error.import", null, locale))
        }
        val currentPage = page.orElse(1)
        val pageSize = size.orElse(5)

        val listaObveza = korisnikServis.stranicenjeTransakcija(
            PageRequest.of(currentPage - 1, pageSize),
            korisnikServis.dohvatiTransakcijeZaKorisnika()
        )
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

    @GetMapping(value = [csvData])
    fun showUserHomePage(
        model: Model,
        @RequestParam("page") page: Optional<Int>,
        @RequestParam("size") size: Optional<Int>
    ): String {
        val currentPage = page.orElse(1)
        val pageSize = size.orElse(5)

        val listaObveza = korisnikServis.stranicenjeTransakcija(
            PageRequest.of(currentPage - 1, pageSize),
            korisnikServis.dohvatiTransakcijeZaKorisnika()
        )
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

    @GetMapping(value = [csvDataImport])
    fun showGetDataImport(
        model: Model,
        @RequestParam("page") page: Optional<Int>,
        @RequestParam("size") size: Optional<Int>
    ): String {
        val currentPage = page.orElse(1)
        val pageSize = size.orElse(5)

        val listaObveza = korisnikServis.stranicenjeTransakcija(
            PageRequest.of(currentPage - 1, pageSize),
            korisnikServis.dohvatiTransakcijeZaKorisnika()
        )
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

    @GetMapping(displayEntries)
    fun displaySearch(): ModelAndView {
        val modelAndView = ModelAndView("ajax")
        modelAndView.addObject("kategorije", kategorijaRepozitorij.findAll())
        modelAndView.addObject("racuni", korisnikServis.dohvatiSveRacuneDostupneKorisniku())
        return modelAndView
    }
}