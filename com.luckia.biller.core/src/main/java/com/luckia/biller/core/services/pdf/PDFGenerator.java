package com.luckia.biller.core.services.pdf;

import java.awt.Color;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.ColumnText;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.luckia.biller.core.i18n.I18nService;
import com.luckia.biller.core.model.AbstractBill;
import com.luckia.biller.core.model.Address;
import com.luckia.biller.core.model.Bill;
import com.luckia.biller.core.model.CommonState;
import com.luckia.biller.core.model.LegalEntity;
import com.luckia.biller.core.model.Store;

public abstract class PDFGenerator<T> {

	private static final Logger LOG = LoggerFactory.getLogger(PDFGenerator.class);

	@Inject
	protected I18nService i18nService;

	protected final Font documentFont;
	protected final Font titleFont;
	protected final Font boldFont;
	protected final Font waterMarkFont;
	protected final String dateFormat;
	protected final String monthFormat;
	protected final Locale locale;

	public PDFGenerator() {
		documentFont = FontFactory.getFont("/fonts/CALIBRI.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED, 12f, Font.NORMAL, Color.BLACK);
		boldFont = FontFactory.getFont("/fonts/CALIBRI.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED, 12f, Font.BOLD, Color.BLACK);
		titleFont = FontFactory.getFont("/fonts/CALIBRI.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED, 16f, Font.BOLD, Color.BLACK);
		waterMarkFont = FontFactory.getFont("/fonts/CALIBRI.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED, 80f, Font.BOLD, new Color(240, 240, 240));
		locale = new Locale("es", "ES");
		dateFormat = "dd-MM-yyyy";
		monthFormat = "MMMM yyyy";
	}

	public abstract void generate(T entity, OutputStream out);

	protected abstract float getLineHeight();

	protected void printTitle(Document document, AbstractBill abstractBill) throws DocumentException {
		String dateFormated = new SimpleDateFormat(dateFormat).format(abstractBill.getBillDate());
		String monthFormated = StringUtils.capitalize(new SimpleDateFormat(monthFormat, locale).format(abstractBill.getBillDate()));
		Boolean isBill = abstractBill.getClass().isAssignableFrom(Bill.class);
		String billTitleLabel = isBill ? i18nService.getMessage("pdf.label.billTitle") : i18nService.getMessage("pdf.label.liquidationTitle");
		String billCodeLabel = isBill ? i18nService.getMessage("pdf.label.billCodeLabel") : i18nService.getMessage("pdf.label.liquidationCodeLabel");
		String billPeriodLabel = isBill ? i18nService.getMessage("pdf.label.billPeriodLabel") : i18nService.getMessage("pdf.label.liquidationPeriodLabel");
		String billDateLabel = i18nService.getMessage("pdf.label.billDate");
		PdfPTable table = new PdfPTable(new float[] { 80f, 20f, 100f });
		table.setWidthPercentage(100f);
		table.addCell(createCell(billCodeLabel, Element.ALIGN_LEFT, documentFont));
		table.addCell(createCell(abstractBill.getCode(), Element.ALIGN_RIGHT, documentFont));
		table.addCell(createCell(StringUtils.EMPTY, Element.ALIGN_RIGHT, documentFont));
		table.addCell(createCell(billDateLabel, Element.ALIGN_LEFT, documentFont));
		table.addCell(createCell(dateFormated, Element.ALIGN_RIGHT, documentFont));
		table.addCell(createCell(StringUtils.EMPTY, Element.ALIGN_RIGHT, documentFont));
		table.addCell(createCell(billPeriodLabel, Element.ALIGN_LEFT, documentFont));
		table.addCell(createCell(monthFormated, Element.ALIGN_RIGHT, documentFont));
		table.addCell(createCell(StringUtils.EMPTY, Element.ALIGN_RIGHT, documentFont));
		Paragraph paragraph = new Paragraph();
		paragraph.setSpacingBefore(25f);
		paragraph.add(new Phrase(billTitleLabel, titleFont));
		paragraph.add(table);
		document.add(paragraph);
	}

	/**
	 * En caso de que la factura o liquidacion esten en estado <code>BillDraft</code> muestra una marca de agua indicando que el documento
	 * es un borrador.
	 * 
	 * @param document
	 * @param writer
	 * @param bill
	 */
	protected void addWaterMark(Document document, PdfWriter writer, AbstractBill bill) {
		if (CommonState.Draft.name().equals(bill.getCurrentState().getStateDefinition().getId())) {
			PdfContentByte canvas = writer.getDirectContent();
			Phrase phrase = new Phrase("BORRADOR", waterMarkFont);
			ColumnText.showTextAligned(canvas, Element.ALIGN_LEFT, phrase, document.getPageSize().getHeight() / 2, 200f, 45f);
		}
	}

	protected void printLegalEntities(Document document, LegalEntity sender, LegalEntity receiver) throws DocumentException {
		if (Store.class.isAssignableFrom(sender.getClass()) && ((Store) sender).getOwner() != null) {
			sender = ((Store) sender).getOwner();
		}
		PdfPTable table = new PdfPTable(new float[] { 40, 40f, 40 });
		table.setWidthPercentage(100f);
		table.getDefaultCell().setBorder(0);
		table.getDefaultCell().setBorderWidth(0f);
		table.addCell(createLegalEntityCell("EMISOR", sender, sender));
		table.addCell(createCell("", Element.ALIGN_RIGHT, documentFont));
		table.addCell(createLegalEntityCell("DESTINATARIO", receiver, null));
		Paragraph paragraph = new Paragraph();
		paragraph.add(table);
		document.add(paragraph);
	}

	protected PdfPCell createLegalEntityCell(String title, LegalEntity legalEntity, LegalEntity child) {
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

	protected String formatAddress(Address address) {
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

	protected PdfPCell createCell(String string, int horizontalAlignment, Font font) {
		return createCell(string, horizontalAlignment, PdfPCell.NO_BORDER, 0, font);
	}

	protected PdfPCell createCell(String string, int horizontalAlignment, int border, int borderWidth, Font font) {
		PdfPCell cell = new PdfPCell();
		Paragraph paragraph = new Paragraph(string, font);
		paragraph.setAlignment(horizontalAlignment);
		cell.setBorder(border);
		cell.addElement(paragraph);
		cell.setHorizontalAlignment(horizontalAlignment);
		cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
		if (border != PdfPCell.NO_BORDER) {
			cell.setBorderWidth(borderWidth);
			cell.setBorderColor(Color.black);

		}
		return cell;
	}

	protected List<PdfPCell> createEmptyCells(int number) {
		List<PdfPCell> list = new ArrayList<PdfPCell>();
		for (int i = 0; i < number; i++) {
			list.add(createCell(StringUtils.EMPTY, Element.ALIGN_RIGHT, documentFont));
		}
		return list;
	}

	protected void printCanvas(Document document, PdfWriter writer, Font font, float x, float y, float maxWidth, List<String> text) {
		PdfContentByte canvas = writer.getDirectContent();
		List<String> splitedLines = new ArrayList<String>();
		for (String str : text) {
			List<String> lines = splitText(str, x, maxWidth, font);
			for (String i : lines) {
				splitedLines.add(i);
			}
		}
		for (String str : splitedLines) {
			Phrase phrase = new Phrase(str, font);
			ColumnText.showTextAligned(canvas, Element.ALIGN_LEFT, phrase, x, y, 0);
			y -= getLineHeight();
		}
	}

	protected List<String> splitText(String line, float x, float maxSize, Font font) {
		List<String> result = new ArrayList<String>();
		Phrase phrase = new Phrase(line, font);
		float phraseWidth = ColumnText.getWidth(phrase);
		if (phraseWidth + x <= maxSize) {
			result.add(line);
			return result;
		} else {
			LOG.trace("Detectada frase que hay que cortar: [" + line + "]");
			LOG.trace(String.format("PhraseWidth: %s, maxSize: %s, y: %s, diff: %s", phraseWidth, maxSize, x, (phraseWidth + x - maxSize)));
			int indexValid = -1;
			String[] subPhrases = line.split(" ");
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < subPhrases.length; i++) {
				if (i != 0) {
					sb.append(" ");
				}
				sb.append(subPhrases[i]);
				Phrase tmpPhrase = new Phrase(sb.toString(), font);
				float tmpWidth = ColumnText.getWidth(tmpPhrase);
				if (x + tmpWidth < maxSize) {
					indexValid = i; // continuamos buscando la frase
				} else {
					break;
				}
			}
			StringBuffer line0 = new StringBuffer();
			if (indexValid == -1 && !"".equals(line.trim())) {
				line0.append(line);
				LOG.info("Frase demasiado larga que no se puede separar: [" + line + "]");
			} else {
				for (int i = 0; i <= indexValid; i++) {
					if (i != 0) {
						line0.append(" ");
					}
					line0.append(subPhrases[i]);
				}
			}
			String line1 = line.substring(line0.length()).trim();
			List<String> lines1 = splitText(line1, x, maxSize, font);
			result.add(line0.toString());
			for (String i : lines1) {
				result.add(i);
			}
			return result;
		}
	}

	protected void addMetaData(Document document, Object entity) {
		// document.addTitle(String.format("Factura %s", bill.getCode()));
		// document.addSubject("-subject-");
		document.addKeywords("Factura, Luckia");
		document.addAuthor("Luckia");
		document.addCreator("lab.cabrera@gmail.com");
	}
}
