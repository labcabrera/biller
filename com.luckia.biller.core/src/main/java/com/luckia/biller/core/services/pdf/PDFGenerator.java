package com.luckia.biller.core.services.pdf;

import java.awt.Color;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.ColumnText;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfWriter;

public abstract class PDFGenerator<T> {

	private static final Logger LOG = LoggerFactory.getLogger(PDFGenerator.class);

	public abstract void generate(T entity, OutputStream out);

	protected abstract float getLineHeight();

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
}
