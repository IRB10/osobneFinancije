package com.diplomski.osobneFinancije.utils

import com.diplomski.osobneFinancije.entiteti.Transakcija
import com.diplomski.osobneFinancije.repozitoriji.KategorijaRepozitorij
import com.diplomski.osobneFinancije.servisi.KorisnikServis
import org.apache.poi.hssf.util.HSSFColor
import org.apache.poi.openxml4j.exceptions.InvalidFormatException
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.FillPatternType
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.springframework.context.MessageSource
import org.springframework.util.ObjectUtils
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.servlet.view.AbstractView
import java.io.IOException
import java.time.LocalDateTime
import java.util.*
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class ExcelView(
    private val messages: MessageSource,
    private val kategorijaRepozitorij: KategorijaRepozitorij,
    private val korisnikServis: KorisnikServis
) : AbstractView() {
    init {
        this.contentType = "application/vnd.ms-excel"
    }

    @Throws(Exception::class)
    override fun renderMergedOutputModel(
        map: Map<String, Any>,
        httpServletRequest: HttpServletRequest,
        httpServletResponse: HttpServletResponse
    ) {

    }

    @Throws(IOException::class)
    fun generateExcel(
        httpServletResponse: HttpServletResponse,
        workbook: Workbook,
        entries: List<Transakcija>,
        locale: Locale
    ) {
        httpServletResponse.setHeader("Content-Disposition", "inline; filename=Obligations.xls")
        val sheet = workbook.createSheet("Obligations")
        sheet.defaultColumnWidth = 30

        val style = workbook.createCellStyle()
        val font = workbook.createFont()
        font.fontName = "Arial"
        style.fillForegroundColor = HSSFColor.HSSFColorPredefined.BLUE.index
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND)
        font.bold = true
        font.color = HSSFColor.HSSFColorPredefined.WHITE.index
        style.setFont(font)

        val zaglavlje = sheet.createRow(0)
        zaglavlje.createCell(0).setCellValue(messages.getMessage("label.obligation.name", null, locale))
        zaglavlje.getCell(0).cellStyle = style
        zaglavlje.createCell(1).setCellValue(messages.getMessage("label.obligation.amount", null, locale))
        zaglavlje.getCell(1).cellStyle = style
        zaglavlje.createCell(2).setCellValue(messages.getMessage("label.obligation.details", null, locale))
        zaglavlje.getCell(2).cellStyle = style
        zaglavlje.createCell(3).setCellValue(messages.getMessage("label.user.date", null, locale))
        zaglavlje.getCell(3).cellStyle = style
        zaglavlje.createCell(4).setCellValue(messages.getMessage("label.user.transaction.from", null, locale))
        zaglavlje.getCell(4).cellStyle = style
        zaglavlje.createCell(5).setCellValue(messages.getMessage("label.user.transaction.to", null, locale))
        zaglavlje.getCell(5).cellStyle = style
        zaglavlje.createCell(6).setCellValue(messages.getMessage("label.obligation.category", null, locale))
        zaglavlje.getCell(6).cellStyle = style

        var rowCount = 1

        for (entry in entries) {
            val transakcijaRedak = sheet.createRow(rowCount++)
            transakcijaRedak.createCell(0).setCellValue(entry.naziv)
            transakcijaRedak.createCell(1).setCellValue(entry.vrijednost)
            transakcijaRedak.createCell(2).setCellValue(entry.opis)
            transakcijaRedak.createCell(3).setCellValue(entry.danPlacanja.toString())
            transakcijaRedak.createCell(4).setCellValue(entry.transakcijaOd)
            transakcijaRedak.createCell(5).setCellValue(entry.transakcijaPrema)
            if (entry.kategorija_id != null) {
                transakcijaRedak.createCell(6).setCellValue(entry.kategorija_id!!.naziv)
            } else {
                transakcijaRedak.createCell(6).setCellValue("")
            }
        }
        httpServletResponse.contentType = this.contentType
        this.renderWorkbook(workbook, httpServletResponse)

    }

    @Throws(IOException::class, InvalidFormatException::class)
    fun importExcel(excelDataFile: MultipartFile): List<Transakcija> {
        val entryList = ArrayList<Transakcija>()
        val workbook = WorkbookFactory.create(excelDataFile.inputStream)
        val worksheet = Objects.requireNonNull(workbook).getSheetAt(0)
        for (i in 1 until worksheet.physicalNumberOfRows) {
            val entry = Transakcija()
            val row = worksheet.getRow(i)
            for (j in 0..4) {
                if (checkIfEmpty(row.getCell(j))) {
                    throw IOException()
                }
            }

            entry.naziv = row.getCell(0).stringCellValue
            entry.vrijednost = row.getCell(1).numericCellValue
            entry.opis = row.getCell(2).stringCellValue
            entry.danPlacanja = LocalDateTime.parse(row.getCell(3).stringCellValue)
            entry.korisnik = korisnikServis.pronadiPoKorisnickomImenu(row.getCell(4).stringCellValue)
            entry.transakcijaOd = row.getCell(4).stringCellValue
            entry.datumKreiranja = LocalDateTime.now()
            if (ObjectUtils.isEmpty(row.getCell(5))) {
                entry.transakcijaPrema = ""
            } else {
                entry.transakcijaPrema = row.getCell(5).stringCellValue
            }
            if (ObjectUtils.isEmpty(row.getCell(6)) || row.getCell(6).stringCellValue == "") {
                entry.kategorija_id = null
            } else {
                entry.kategorija_id = kategorijaRepozitorij.findByNaziv(row.getCell(6).stringCellValue)
            }
            entryList.add(entry)
        }
        return entryList
    }

    @Throws(IOException::class)
    private fun renderWorkbook(workbook: Workbook, response: HttpServletResponse) {
        val out = response.outputStream
        workbook.write(out)
        workbook.close()
    }

    private fun checkIfEmpty(cell: Cell): Boolean {
        return ObjectUtils.isEmpty(cell)
    }
}