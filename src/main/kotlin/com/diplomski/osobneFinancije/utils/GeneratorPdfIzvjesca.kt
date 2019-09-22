package com.diplomski.osobneFinancije.utils

import com.diplomski.osobneFinancije.entiteti.Transakcija
import com.itextpdf.text.*
import com.itextpdf.text.pdf.PdfPCell
import com.itextpdf.text.pdf.PdfPTable
import com.itextpdf.text.pdf.PdfWriter
import org.springframework.context.MessageSource
import org.springframework.util.ObjectUtils
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.*


class GeneratorPdfIzvjesca {
    companion object {
        @JvmStatic
        fun entriesReport(entries: List<Transakcija>, messages: MessageSource, locale: Locale): ByteArrayInputStream {
            val document = Document()
            val out = ByteArrayOutputStream()

            try {
                val table = PdfPTable(6)
                table.headerRows = 1
                table.widthPercentage = 90f
                table.setWidths(intArrayOf(4, 3, 3, 3, 3, 3))

                val headFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD)

                var hcell: PdfPCell
                hcell = PdfPCell(Phrase(messages.getMessage("label.obligation.name", null, locale), headFont))
                hcell.horizontalAlignment = Element.ALIGN_CENTER
                hcell.backgroundColor = BaseColor.GRAY
                table.addCell(hcell)

                hcell = PdfPCell(Phrase(messages.getMessage("label.obligation.amount", null, locale), headFont))
                hcell.horizontalAlignment = Element.ALIGN_CENTER
                hcell.backgroundColor = BaseColor.GRAY
                table.addCell(hcell)

                hcell = PdfPCell(Phrase(messages.getMessage("label.obligation.details", null, locale), headFont))
                hcell.horizontalAlignment = Element.ALIGN_CENTER
                hcell.backgroundColor = BaseColor.GRAY
                table.addCell(hcell)

                hcell = PdfPCell(Phrase(messages.getMessage("label.user.date", null, locale), headFont))
                hcell.horizontalAlignment = Element.ALIGN_CENTER
                hcell.backgroundColor = BaseColor.GRAY
                table.addCell(hcell)

                hcell = PdfPCell(Phrase(messages.getMessage("label.user.transaction.from", null, locale), headFont))
                hcell.horizontalAlignment = Element.ALIGN_CENTER
                hcell.backgroundColor = BaseColor.GRAY
                table.addCell(hcell)

                hcell = PdfPCell(Phrase(messages.getMessage("label.user.transaction.to", null, locale), headFont))
                hcell.horizontalAlignment = Element.ALIGN_CENTER
                hcell.backgroundColor = BaseColor.GRAY
                table.addCell(hcell)

                for (entry in entries) {
                    var cell: PdfPCell

                    cell = PdfPCell(Phrase(entry.naziv))
                    cell.verticalAlignment = Element.ALIGN_MIDDLE
                    cell.horizontalAlignment = Element.ALIGN_CENTER
                    table.addCell(cell)

                    cell = PdfPCell(Phrase(entry.vrijednost.toString()))
                    cell.paddingLeft = 5f
                    cell.verticalAlignment = Element.ALIGN_MIDDLE
                    cell.horizontalAlignment = Element.ALIGN_CENTER
                    table.addCell(cell)

                    cell = PdfPCell(Phrase(entry.opis))
                    cell.verticalAlignment = Element.ALIGN_MIDDLE
                    cell.horizontalAlignment = Element.ALIGN_CENTER
                    cell.paddingRight = 5f
                    table.addCell(cell)

                    cell = PdfPCell(Phrase(entry.danPlacanja.toString()))
                    cell.paddingLeft = 5f
                    cell.verticalAlignment = Element.ALIGN_MIDDLE
                    cell.horizontalAlignment = Element.ALIGN_CENTER
                    table.addCell(cell)

                    cell = PdfPCell(Phrase(entry.transakcijaOd))
                    cell.verticalAlignment = Element.ALIGN_MIDDLE
                    cell.horizontalAlignment = Element.ALIGN_CENTER
                    cell.paddingRight = 5f
                    table.addCell(cell)

                    if (ObjectUtils.isEmpty(entry.transakcijaPrema)) {
                        cell = PdfPCell(Phrase("----"))
                    } else {
                        cell = PdfPCell(Phrase(entry.transakcijaPrema))
                    }
                    cell.verticalAlignment = Element.ALIGN_MIDDLE
                    cell.horizontalAlignment = Element.ALIGN_CENTER
                    cell.paddingLeft = 5f
                    table.addCell(cell)
                }

                PdfWriter.getInstance(document, out)
                document.open()
                document.add(table)
                document.close()

            } catch (ex: DocumentException) {
                println("Exception PDF:$ex")
            }

            return ByteArrayInputStream(out.toByteArray())
        }
    }
}