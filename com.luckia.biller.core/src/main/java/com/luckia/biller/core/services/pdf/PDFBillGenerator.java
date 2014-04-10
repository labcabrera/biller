package com.luckia.biller.core.services.pdf;

import java.awt.Color;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.luckia.biller.core.model.Bill;
import com.luckia.biller.core.model.BillDetail;

public class PDFBillGenerator extends PDFGenerator<Bill> {

	private static final Logger LOG = LoggerFactory.getLogger(PDFBillGenerator.class);

	@Override
	public void generate(Bill bill, OutputStream out) {
		LOG.debug("Generando PDF de la factura {}", bill.getCode());
		try {
			Rectangle rectangle = PageSize.A3;
			Document document = new Document(rectangle, 50f, 50f, 50f, 50f);
			PdfWriter writer = PdfWriter.getInstance(document, out);
			addMetaData(document, bill);
			document.open();
			addWaterMark(document, writer, bill);

			float halfTop = document.getPageSize().getTop() / 2f;

			// Primera copia
			printLegalEntities(document, bill.getSender(), bill.getReceiver());
			printTitle(document, bill);
			printDetails(document, bill);

			// Segunda copia
			float currentVP = writer.getVerticalPosition(true);
			float vpDiff = currentVP - halfTop;

			printLegalEntities(document, bill.getSender(), bill.getReceiver(), vpDiff);
			printTitle(document, bill);
			printDetails(document, bill);

			// Linea a mitad de pagina para separar la segunda copia
			PdfContentByte cb = writer.getDirectContent();
			cb.setLineWidth(1.0f);
			cb.setColorFill(Color.RED);
			cb.setLineDash(new float[] { 2f, 3f }, 1f);
			float x = 0f;
			float y = halfTop;
			cb.moveTo(x, y);
			cb.lineTo(document.getPageSize().getRight(), halfTop);
			cb.stroke();

			printCanvas(document, writer, smallFont, 370f, halfTop + 10f, 1000f, Arrays.asList("Ejemplar para el establecimiento"));
			printCanvas(document, writer, smallFont, 370f, 10f, 1000f, Arrays.asList("Ejemplar para el operador"));

			document.close();
		} catch (Exception ex) {
			LOG.error("Error al generar la factura", ex);
		}
	}

	private void printDetails(Document document, Bill bill) throws DocumentException {
		PdfPTable table = new PdfPTable(new float[] { 30f, 10f, 10f, 10f, 10f });
		table.setWidthPercentage(100f);

		List<PdfPCell> cells = new ArrayList<PdfPCell>();

		cells.add(createCell("Detalles", Element.ALIGN_LEFT, documentFont));
		cells.addAll(createEmptyCells(4));

		cells.add(createCell("Descripción", Element.ALIGN_LEFT, boldFont));
		cells.add(createCell("Cantidad", Element.ALIGN_RIGHT, boldFont));
		cells.add(createCell("Precio unidad", Element.ALIGN_RIGHT, boldFont));
		cells.add(createCell("Desc. (%)", Element.ALIGN_RIGHT, boldFont));
		cells.add(createCell("Importe", Element.ALIGN_RIGHT, boldFont));
		for (BillDetail detail : bill.getDetails()) {
			cells.add(createCell(detail.getName(), Element.ALIGN_LEFT, documentFont));
			cells.add(createCell(String.valueOf(detail.getUnits()), Element.ALIGN_RIGHT, documentFont));
			cells.add(createCell("-", Element.ALIGN_RIGHT, documentFont));
			cells.add(createCell("-", Element.ALIGN_RIGHT, documentFont));
			cells.add(createCell(detail.getValue() + " €", Element.ALIGN_RIGHT, documentFont));
		}

		cells.addAll(createEmptyCells(4));

		cells.addAll(createEmptyCells(3));
		cells.add(createCell("Base", Element.ALIGN_RIGHT, documentFont));
		cells.addAll(createEmptyCells(1));
		cells.add(createCell(bill.getNetAmount().toString() + " €", Element.ALIGN_RIGHT, documentFont));

		cells.addAll(createEmptyCells(2));
		cells.add(createCell(String.format("IVA (%s%%)", bill.getVatPercent()), Element.ALIGN_RIGHT, documentFont));
		cells.addAll(createEmptyCells(1));
		cells.add(createCell(bill.getVatAmount().toString() + " €", Element.ALIGN_RIGHT, documentFont));

		cells.add(createCell("TOTAL", Element.ALIGN_LEFT, boldFont));
		cells.addAll(createEmptyCells(3));
		cells.add(createCell(bill.getAmount().toString() + " €", Element.ALIGN_RIGHT, boldFont));

		int cols = 5;
		for (int i = 0; i < cells.size(); i++) {
			int col = i % cols;
			int row = i / cols;
			PdfPCell cell = cells.get(i);
			cell.setPaddingLeft(8f);
			cell.setPaddingRight(8f);
			cell.setPaddingBottom(3f);
			cell.setPaddingTop(3f);
			if (col == 0) {
				cell.setBorderWidthLeft(2f);
			} else if (col == 4) {
				cell.setBorderWidthRight(2f);
			}
			if (row == 0) {
				cell.setBorderWidthTop(2f);
			} else if (row == 1) {
				cell.setBorderWidthBottom(0.5f);
				cell.setPaddingBottom(8f);
			} else if ((row == (cells.size() / cols) - 4) /* && (col == 3 || col == 4) */) {
				cell.setBorderWidthBottom(0.5f);
			} else if (row == (cells.size() / cols) - 2) {
				cell.setBorderWidthBottom(0.5f);
			} else if (row == (cells.size() / cols) - 1) {
				cell.setBorderWidthBottom(2f);
				cell.setPaddingBottom(8f);
			}
		}
		for (PdfPCell cell : cells) {
			table.addCell(cell);
		}
		Paragraph paragraph = new Paragraph();
		paragraph.setSpacingBefore(25f);
		paragraph.add(table);
		document.add(paragraph);
	}

	@Override
	protected float getLineHeight() {
		return 20f;
	}
}
