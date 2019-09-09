package com.diplomski.osobneFinancije.utils

import com.diplomski.osobneFinancije.entiteti.Transakcija
import com.itextpdf.text.*
import com.itextpdf.text.pdf.PdfPCell
import com.itextpdf.text.pdf.PdfPTable
import com.itextpdf.text.pdf.PdfWriter
import org.springframework.util.ObjectUtils
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream


class GeneratorPdfIzvjesca {
    companion object {
        @JvmStatic
        public fun entriesReport(entries: List<Transakcija>): ByteArrayInputStream {
            val document = Document()
            val out = ByteArrayOutputStream()

            try {
                val table = PdfPTable(6)
                table.widthPercentage = 90f
                table.setWidths(intArrayOf(4, 3, 3, 3, 3, 3))

                val headFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD)

                var hcell: PdfPCell
                hcell = PdfPCell(Phrase("Obligation naziv", headFont))
                hcell.horizontalAlignment = Element.ALIGN_CENTER
                table.addCell(hcell)

                hcell = PdfPCell(Phrase("Amount", headFont))
                hcell.horizontalAlignment = Element.ALIGN_CENTER
                table.addCell(hcell)

                hcell = PdfPCell(Phrase("Obligation details", headFont))
                hcell.horizontalAlignment = Element.ALIGN_CENTER
                table.addCell(hcell)

                hcell = PdfPCell(Phrase("Obligation date", headFont))
                hcell.horizontalAlignment = Element.ALIGN_CENTER
                table.addCell(hcell)

                hcell = PdfPCell(Phrase("Transaction from", headFont))
                hcell.horizontalAlignment = Element.ALIGN_CENTER
                table.addCell(hcell)

                hcell = PdfPCell(Phrase("Transaction to", headFont))
                hcell.horizontalAlignment = Element.ALIGN_CENTER
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