package com.luckia.biller.core.services.pdf;

import java.awt.Color;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.ColumnText;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.luckia.biller.core.model.Address;
import com.luckia.biller.core.model.Bill;
import com.luckia.biller.core.model.BillDetail;
import com.luckia.biller.core.model.BillState;
import com.luckia.biller.core.model.LegalEntity;
import com.luckia.biller.core.model.Store;

public class PDFBillGenerator extends PDFGenerator<Bill> {

	private static final Logger LOG = LoggerFactory.getLogger(PDFBillGenerator.class);

	private final Font documentFont;
	private final Font titleFont;
	private final Font boldFont;
	private final Font waterMarkFont;
	private final String dateFormat;
	private final String monthFormat;
	private final Locale locale;

	public PDFBillGenerator() {
		documentFont = FontFactory.getFont("/fonts/CALIBRI.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED, 12f, Font.NORMAL, Color.BLACK);
		boldFont = FontFactory.getFont("/fonts/CALIBRI.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED, 12f, Font.BOLD, Color.BLACK);
		titleFont = FontFactory.getFont("/fonts/CALIBRI.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED, 16f, Font.BOLD, Color.BLACK);
		waterMarkFont = FontFactory.getFont("/fonts/CALIBRI.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED, 80f, Font.BOLD, new Color(240, 240, 240));
		locale = new Locale("es", "ES");
		dateFormat = "dd-MM-yyyy";
		monthFormat = "MMMM yyyy";
	}

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
			printLegalEntities(document, bill);
			printTitle(document, bill);
			printDetails(document, bill);
			document.close();
		} catch (Exception ex) {
			LOG.error("Error al generar la factura", ex);
		}
	}

	private void printLegalEntities(Document document, Bill bill) throws DocumentException {
		LegalEntity sender = bill.getSender();
		LegalEntity receiver = bill.getReceiver();
		if (Store.class.isAssignableFrom(bill.getSender().getClass()) && ((Store) bill.getSender()).getOwner() != null) {
			sender = ((Store) bill.getSender()).getOwner();
		}
		PdfPTable table = new PdfPTable(new float[] { 40, 40f, 40 });
		table.setWidthPercentage(100f);
		table.getDefaultCell().setBorder(0);
		table.getDefaultCell().setBorderWidth(0f);
		table.addCell(createLegalEntityCell("EMISOR", sender, bill.getSender()));
		table.addCell(createCell("", Element.ALIGN_RIGHT, documentFont));
		table.addCell(createLegalEntityCell("DESTINATARIO", receiver, null));
		Paragraph paragraph = new Paragraph();
		paragraph.add(table);
		document.add(paragraph);
	}

	private PdfPCell createLegalEntityCell(String title, LegalEntity legalEntity, LegalEntity child) {
		PdfPCell cell = new PdfPCell();
		cell.setBorder(0);
		cell.addElement(new Paragraph(new Phrase(title, boldFont)));
		cell.addElement(new Paragraph(new Phrase("Nombre: " + legalEntity.getName(), documentFont)));
		if (legalEntity.getAddress() != null) {
			cell.addElement(new Paragraph(new Phrase("Dirección:\n" + formatAddress(legalEntity.getAddress()), documentFont)));
		}
		if (legalEntity.getIdCard() != null && StringUtils.isNotBlank(legalEntity.getIdCard().getNumber())) {
			cell.addElement(new Paragraph(new Phrase("NIF/CIF: " + legalEntity.getIdCard().getNumber(), documentFont)));
		}
		if (child != null && child.getId() != legalEntity.getId()) {
			cell.addElement(new Paragraph(new Phrase("LOCAL:", boldFont)));
			cell.addElement(new Paragraph(new Phrase("Nombre: " + child.getName(), documentFont)));
			if (child.getAddress() != null) {
				cell.addElement(new Paragraph(new Phrase("Dirección: " + child.getAddress().getRoad(), documentFont)));
			}
			if (child.getIdCard() != null && StringUtils.isNotBlank(child.getIdCard().getNumber())) {
				cell.addElement(new Paragraph(new Phrase("NIF/CIF: " + child.getIdCard().getNumber(), documentFont)));
			}
		}
		return cell;
	}

	private String formatAddress(Address address) {
		StringBuffer sb = new StringBuffer();
		if (StringUtils.isNotBlank(address.getRoad())) {
			sb.append(address.getRoad()).append(" ");
		}
		if (StringUtils.isNotBlank(address.getNumber())) {
			sb.append(address.getNumber());
		}
		sb.append("\n");
		if (StringUtils.isNotEmpty(address.getZipCode())) {
			sb.append(address.getZipCode());
		}
		if (address.getRegion() != null) {
			sb.append(address.getRegion().getName()).append(" ");
		}
		if (address.getProvince() != null) {
			sb.append(address.getProvince().getName()).append(" ");
		}
		return sb.toString();
	}

	private void printTitle(Document document, Bill bill) throws DocumentException {
		PdfPTable table = new PdfPTable(new float[] { 80f, 20f, 100f });
		table.setWidthPercentage(100f);
		table.addCell(createCell("Número de factura", Element.ALIGN_LEFT, documentFont));
		table.addCell(createCell(bill.getCode(), Element.ALIGN_RIGHT, documentFont));
		table.addCell(createCell("", Element.ALIGN_RIGHT, documentFont));
		table.addCell(createCell("Fecha de emisión", Element.ALIGN_LEFT, documentFont));
		table.addCell(createCell(new SimpleDateFormat(dateFormat).format(bill.getBillDate()), Element.ALIGN_RIGHT, documentFont));
		table.addCell(createCell("", Element.ALIGN_RIGHT, documentFont));
		table.addCell(createCell("Periodo de facturación", Element.ALIGN_LEFT, documentFont));
		table.addCell(createCell(new SimpleDateFormat(monthFormat, locale).format(bill.getBillDate()), Element.ALIGN_RIGHT, documentFont));
		table.addCell(createCell("", Element.ALIGN_RIGHT, documentFont));
		Paragraph paragraph = new Paragraph();
		paragraph.setSpacingBefore(25f);
		paragraph.add(new Phrase("FACTURA MÁQUINA DE APUESTAS", titleFont));
		paragraph.add(table);
		document.add(paragraph);
	}

	private void printDetails(Document document, Bill bill) throws DocumentException {
		PdfPTable table = new PdfPTable(new float[] { 30f, 10f, 10f, 10f, 10f });
		table.setWidthPercentage(100f);

		List<PdfPCell> cells = new ArrayList<PdfPCell>();

		cells.add(createCell("Titulo por definir", Element.ALIGN_LEFT, documentFont));
		cells.add(createCell("", Element.ALIGN_RIGHT, documentFont));
		cells.add(createCell("", Element.ALIGN_RIGHT, documentFont));
		cells.add(createCell("", Element.ALIGN_RIGHT, documentFont));
		cells.add(createCell("", Element.ALIGN_RIGHT, documentFont));

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
		cells.add(createCell("", Element.ALIGN_RIGHT, documentFont));
		cells.add(createCell("", Element.ALIGN_RIGHT, documentFont));
		cells.add(createCell("", Element.ALIGN_RIGHT, documentFont));
		cells.add(createCell("", Element.ALIGN_RIGHT, documentFont));
		cells.add(createCell("", Element.ALIGN_RIGHT, documentFont));

		cells.add(createCell("", Element.ALIGN_RIGHT, documentFont));
		cells.add(createCell("", Element.ALIGN_RIGHT, documentFont));
		cells.add(createCell("", Element.ALIGN_RIGHT, documentFont));
		cells.add(createCell("Base", Element.ALIGN_RIGHT, documentFont));
		cells.add(createCell(bill.getNetAmount().toString() + " €", Element.ALIGN_RIGHT, documentFont));

		cells.add(createCell("", Element.ALIGN_RIGHT, documentFont));
		cells.add(createCell("", Element.ALIGN_RIGHT, documentFont));
		cells.add(createCell("", Element.ALIGN_RIGHT, documentFont));
		cells.add(createCell(String.format("IVA (%s%%)", bill.getVatPercent()), Element.ALIGN_RIGHT, documentFont));
		cells.add(createCell(bill.getVatAmount().toString() + " €", Element.ALIGN_RIGHT, documentFont));

		cells.add(createCell("TOTAL", Element.ALIGN_LEFT, boldFont));
		cells.add(createCell("", Element.ALIGN_RIGHT, documentFont));
		cells.add(createCell("", Element.ALIGN_RIGHT, documentFont));
		cells.add(createCell("", Element.ALIGN_RIGHT, documentFont));
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

	private void addWaterMark(Document document, PdfWriter writer, Bill bill) {
		if (BillState.BillDraft.name().equals(bill.getCurrentState().getStateDefinition().getId())) {
			PdfContentByte canvas = writer.getDirectContent();
			Phrase phrase = new Phrase("BORRADOR", waterMarkFont);
			ColumnText.showTextAligned(canvas, Element.ALIGN_LEFT, phrase, document.getPageSize().getHeight() / 2, 200f, 45f);
		}
	}

	private void addMetaData(Document document, Bill bill) {
		document.addTitle(String.format("Factura %s", bill.getCode()));
		document.addSubject("-subject-");
		document.addKeywords("Factura, Luckia");
		document.addAuthor("Luckia");
		document.addCreator("lab.cabrera@gmail.com");
	}

	@Override
	protected float getLineHeight() {
		return 20f;
	}
}
