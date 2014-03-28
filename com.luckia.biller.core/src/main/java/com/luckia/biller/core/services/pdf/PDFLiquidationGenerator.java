package com.luckia.biller.core.services.pdf;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
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
import com.luckia.biller.core.model.Liquidation;

public class PDFLiquidationGenerator extends PDFGenerator<Liquidation> {

	private static final Logger LOG = LoggerFactory.getLogger(PDFLiquidationGenerator.class);

	@Override
	public void generate(Liquidation liquidation, OutputStream out) {
		LOG.debug("Generando PDF de la liquidacion {}", liquidation.getCode());
		try {
			Rectangle rectangle = PageSize.A3.rotate();
			Document document = new Document(rectangle, 50f, 50f, 50f, 50f);
			PdfWriter writer = PdfWriter.getInstance(document, out);
			addMetaData(document, liquidation);
			document.open();
			addWaterMark(document, writer, liquidation);
			printLegalEntities(document, liquidation.getSender(), liquidation.getReceiver());
			printTitle(document, liquidation);
			printDetails(document, liquidation);
			document.newPage();
			document.close();
		} catch (Exception ex) {
			LOG.error("Error al generar el PDF de la liquidacion", ex);
		}
	}

	private void printDetails(Document document, Liquidation liquidation) throws DocumentException {
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

		cells.add(createCell("A) Honorarios por apuestas", Element.ALIGN_LEFT, documentFont));
		cells.add(createCell("1", Element.ALIGN_RIGHT, documentFont));
		cells.add(createCell(StringUtils.EMPTY, Element.ALIGN_RIGHT, documentFont));
		cells.add(createCell(StringUtils.EMPTY, Element.ALIGN_RIGHT, documentFont));
		cells.add(createCell("???", Element.ALIGN_RIGHT, documentFont));

		cells.add(createCell("B) Honorarios para bares", Element.ALIGN_LEFT, documentFont));
		cells.add(createCell("1", Element.ALIGN_RIGHT, documentFont));
		cells.add(createCell(StringUtils.EMPTY, Element.ALIGN_RIGHT, documentFont));
		cells.add(createCell(StringUtils.EMPTY, Element.ALIGN_RIGHT, documentFont));
		cells.add(createCell("???", Element.ALIGN_RIGHT, documentFont));

		cells.add(createCell("C) Honorarios SAT", Element.ALIGN_LEFT, documentFont));
		cells.add(createCell("1", Element.ALIGN_RIGHT, documentFont));
		cells.add(createCell(StringUtils.EMPTY, Element.ALIGN_RIGHT, documentFont));
		cells.add(createCell(StringUtils.EMPTY, Element.ALIGN_RIGHT, documentFont));
		cells.add(createCell("???", Element.ALIGN_RIGHT, documentFont));

		cells.add(createCell("D) Ajustes operativos", Element.ALIGN_LEFT, documentFont));
		cells.add(createCell("1", Element.ALIGN_RIGHT, documentFont));
		cells.add(createCell(StringUtils.EMPTY, Element.ALIGN_RIGHT, documentFont));
		cells.add(createCell(StringUtils.EMPTY, Element.ALIGN_RIGHT, documentFont));
		cells.add(createCell("???", Element.ALIGN_RIGHT, documentFont));

		cells.add(createCell("TOTAL LIQUIDACION", Element.ALIGN_LEFT, boldFont));
		cells.add(createCell(StringUtils.EMPTY, Element.ALIGN_RIGHT, documentFont));
		cells.add(createCell(StringUtils.EMPTY, Element.ALIGN_RIGHT, documentFont));
		cells.add(createCell(StringUtils.EMPTY, Element.ALIGN_RIGHT, documentFont));
		cells.add(createCell(liquidation.getAmount().toString() + " €", Element.ALIGN_RIGHT, documentFont));

		cells.add(createCell(StringUtils.EMPTY, Element.ALIGN_RIGHT, documentFont));
		cells.add(createCell(StringUtils.EMPTY, Element.ALIGN_RIGHT, documentFont));
		cells.add(createCell(StringUtils.EMPTY, Element.ALIGN_RIGHT, documentFont));
		cells.add(createCell(StringUtils.EMPTY, Element.ALIGN_RIGHT, documentFont));
		cells.add(createCell(StringUtils.EMPTY, Element.ALIGN_RIGHT, documentFont));

		cells.add(createCell("Recaudación en posesión de XXXX", Element.ALIGN_LEFT, documentFont));
		cells.add(createCell(StringUtils.EMPTY, Element.ALIGN_RIGHT, documentFont));
		cells.add(createCell(StringUtils.EMPTY, Element.ALIGN_RIGHT, documentFont));
		cells.add(createCell(StringUtils.EMPTY, Element.ALIGN_RIGHT, documentFont));
		cells.add(createCell("???", Element.ALIGN_RIGHT, documentFont));

		cells.add(createCell("Ajustes operativos (100%)", Element.ALIGN_LEFT, documentFont));
		cells.add(createCell(StringUtils.EMPTY, Element.ALIGN_RIGHT, documentFont));
		cells.add(createCell(StringUtils.EMPTY, Element.ALIGN_RIGHT, documentFont));
		cells.add(createCell(StringUtils.EMPTY, Element.ALIGN_RIGHT, documentFont));
		cells.add(createCell("???", Element.ALIGN_RIGHT, documentFont));

		cells.add(createCell("Recaudación en posesión de XXXX", Element.ALIGN_LEFT, documentFont));
		cells.add(createCell(StringUtils.EMPTY, Element.ALIGN_RIGHT, documentFont));
		cells.add(createCell(StringUtils.EMPTY, Element.ALIGN_RIGHT, documentFont));
		cells.add(createCell(StringUtils.EMPTY, Element.ALIGN_RIGHT, documentFont));
		cells.add(createCell("???", Element.ALIGN_RIGHT, documentFont));

		cells.add(createCell("Total liquidación a percibir por XXXX", Element.ALIGN_LEFT, documentFont));
		cells.add(createCell(StringUtils.EMPTY, Element.ALIGN_RIGHT, documentFont));
		cells.add(createCell(StringUtils.EMPTY, Element.ALIGN_RIGHT, documentFont));
		cells.add(createCell(StringUtils.EMPTY, Element.ALIGN_RIGHT, documentFont));
		cells.add(createCell("???", Element.ALIGN_RIGHT, documentFont));

		cells.add(createCell("Total a ingresar a XXXXX (XXXX)", Element.ALIGN_LEFT, boldFont));
		cells.add(createCell(StringUtils.EMPTY, Element.ALIGN_RIGHT, documentFont));
		cells.add(createCell(StringUtils.EMPTY, Element.ALIGN_RIGHT, documentFont));
		cells.add(createCell(StringUtils.EMPTY, Element.ALIGN_RIGHT, documentFont));
		cells.add(createCell(liquidation.getAmount().toString() + "€", Element.ALIGN_RIGHT, boldFont));

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
			} else if (row == (cells.size() / cols) - 8) { // linea antes de mostrar el total
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
		return 20;
	}
}
