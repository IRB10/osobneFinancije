package com.diplomski.osobneFinancije.utils

import com.diplomski.osobneFinancije.entiteti.Transakcija
import org.apache.poi.hssf.util.HSSFColor
import org.apache.poi.openxml4j.exceptions.InvalidFormatException
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.FillPatternType
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.springframework.util.ObjectUtils
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.servlet.view.AbstractView
import java.io.IOException
import java.time.LocalDateTime
import java.util.*
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class ExcelView : AbstractView() {
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
    fun generateExcel(httpServletResponse: HttpServletResponse, workbook: Workbook, entries: List<Transakcija>) {
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

        val header = sheet.createRow(0)
        header.createCell(0).setCellValue("Obligation name")
        header.getCell(0).cellStyle = style
        header.createCell(1).setCellValue("Amount")
        header.getCell(1).cellStyle = style
        header.createCell(2).setCellValue("Obligation details")
        header.getCell(2).cellStyle = style
        header.createCell(3).setCellValue("Obligation date")
        header.getCell(3).cellStyle = style
        header.createCell(4).setCellValue("Transaction from")
        header.getCell(4).cellStyle = style
        header.createCell(5).setCellValue("Transaction to")
        header.getCell(5).cellStyle = style

        var rowCount = 1

        for (entry in entries) {
            val userRow = sheet.createRow(rowCount++)
            userRow.createCell(0).setCellValue(entry.naziv)
            userRow.createCell(1).setCellValue(entry.vrijednost)
            userRow.createCell(2).setCellValue(entry.opis)
            userRow.createCell(3).setCellValue(entry.danPlacanja.toString())
            userRow.createCell(4).setCellValue(entry.transakcijaOd)
            userRow.createCell(5).setCellValue(entry.transakcijaPrema)
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

            entry.kategorija_id!!.naziv = row.getCell(0).stringCellValue
            entry.vrijednost = row.getCell(1).numericCellValue
            entry.opis = row.getCell(2).stringCellValue
            entry.danPlacanja = LocalDateTime.parse(row.getCell(3).stringCellValue)
            entry.transakcijaOd = row.getCell(4).stringCellValue
            if (ObjectUtils.isEmpty(row.getCell(5))) {
                entry.transakcijaPrema = ""
            } else {
                entry.transakcijaPrema = row.getCell(5).stringCellValue
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