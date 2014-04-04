package com.luckia.biller.core.services.pdf;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Rectangle;
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
			Rectangle rectangle = PageSize.A3.rotate();
			Document document = new Document(rectangle, 50f, 50f, 50f, 50f);
			PdfWriter writer = PdfWriter.getInstance(document, out);
			addMetaData(document, bill);
			document.open();
			addWaterMark(document, writer, bill);
			printLegalEntities(document, bill.getSender(), bill.getReceiver());
			printTitle(document, bill);
			printDetails(document, bill);
			document.close();
		} catch (Exception ex) {
			LOG.error("Error al generar la factura", ex);
		}
	}

	private void printDetails(Document document, Bill bill) throws DocumentException {
		PdfPTable table = new PdfPTable(new float[] { 30f, 10f, 10f, 10f, 10f });
		table.setWidthPercentage(100f);

		List<PdfPCell> cells = new ArrayList<PdfPCell>();

		cells.add(createCell("Titulo por definir", Element.ALIGN_LEFT, documentFont));
		cells.add(createCell(StringUtils.EMPTY, Element.ALIGN_RIGHT, documentFont));
		cells.add(createCell(StringUtils.EMPTY, Element.ALIGN_RIGHT, documentFont));
		cells.add(createCell(StringUtils.EMPTY, Element.ALIGN_RIGHT, documentFont));
		cells.add(createCell(StringUtils.EMPTY, Element.ALIGN_RIGHT, documentFont));

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
		cells.add(createCell(StringUtils.EMPTY, Element.ALIGN_RIGHT, documentFont));
		cells.add(createCell(StringUtils.EMPTY, Element.ALIGN_RIGHT, documentFont));
		cells.add(createCell(StringUtils.EMPTY, Element.ALIGN_RIGHT, documentFont));
		cells.add(createCell(StringUtils.EMPTY, Element.ALIGN_RIGHT, documentFont));
		cells.add(createCell(StringUtils.EMPTY, Element.ALIGN_RIGHT, documentFont));

		cells.add(createCell(StringUtils.EMPTY, Element.ALIGN_RIGHT, documentFont));
		cells.add(createCell(StringUtils.EMPTY, Element.ALIGN_RIGHT, documentFont));
		cells.add(createCell(StringUtils.EMPTY, Element.ALIGN_RIGHT, documentFont));
		cells.add(createCell("Base", Element.ALIGN_RIGHT, documentFont));
		cells.add(createCell(bill.getNetAmount().toString() + " €", Element.ALIGN_RIGHT, documentFont));

		cells.add(createCell(StringUtils.EMPTY, Element.ALIGN_RIGHT, documentFont));
		cells.add(createCell(StringUtils.EMPTY, Element.ALIGN_RIGHT, documentFont));
		cells.add(createCell(StringUtils.EMPTY, Element.ALIGN_RIGHT, documentFont));
		cells.add(createCell(String.format("IVA (%s%%)", bill.getVatPercent()), Element.ALIGN_RIGHT, documentFont));
		cells.add(createCell(bill.getVatAmount().toString() + " €", Element.ALIGN_RIGHT, documentFont));

		cells.add(createCell("TOTAL", Element.ALIGN_LEFT, boldFont));
		cells.add(createCell(StringUtils.EMPTY, Element.ALIGN_RIGHT, documentFont));
		cells.add(createCell(StringUtils.EMPTY, Element.ALIGN_RIGHT, documentFont));
		cells.add(createCell(StringUtils.EMPTY, Element.ALIGN_RIGHT, documentFont));
		cells.add(createCell(bill.getAmount().toString() + " €", Element.ALIGN_RIGHT, boldFont));

		int cols = 5;
		for (int i = 0; i < cells.size(); i++) {
			int col = i % cols;
			int row = i / cols;
			PdfPCell cell = cells.get(i);
			cell.setPadding(8f);
			if (col == 0) {
				cell.setBorderWidthLeft(2f);
			} else if (col == 4) {
				cell.setBorderWidthRight(2f);
			}
			if (row == 0) {
				cell.setBorderWidthTop(2f);
			} else if (row == 1) {
				cell.setBorderWidthBottom(1f);
			} else if ((row == (cells.size() / cols) - 4) && (col == 3 || col == 4)) {
				cell.setBorderWidthBottom(1f);
			} else if (row == (cells.size() / cols) - 2) {
				cell.setBorderWidthBottom(1f);
			} else if (row == (cells.size() / cols) - 1) {
				cell.setBorderWidthBottom(2f);
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
