package com.diplomski.osobneFinancije.kontrolori

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
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.ui.Model
import java.io.IOException
import javax.servlet.http.HttpServletRequest
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.PostMapping
import javax.servlet.http.HttpServletResponse
import org.springframework.context.MessageSource
import org.springframework.stereotype.Controller


@Controller
class PodaciKontrolor(private val korisnikServis: KorisnikServis, @param:Qualifier("messageSource") private val messages: MessageSource) {

    @PostMapping(value = [csvData])
    @Throws(IOException::class)
    fun exportEntries(response: HttpServletResponse) {
        val excelView = ExcelView()
        excelView.generateExcel(response, HSSFWorkbook(), korisnikServis.dohvatiTransakcijeZaKorisnika())
    }

    @PostMapping(value = [csvDataImport])
    fun importEntries(
        @RequestParam("file") excelDataFile: MultipartFile, model: Model,
        request: HttpServletRequest
    ): String {
        val locale = request.locale
        val excelView = ExcelView()
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

        model.addAttribute("obligationList", korisnikServis.dohvatiTransakcije())
        return overview
    }

    @GetMapping(value = [csvData])
    fun showUserHomePage(model: Model): String {
        model.addAttribute("obligationList", korisnikServis.dohvatiTransakcije())
        return overview
    }

    @GetMapping(displayEntries)
    fun displaySearch(): String {
        return "ajax"
    }
}