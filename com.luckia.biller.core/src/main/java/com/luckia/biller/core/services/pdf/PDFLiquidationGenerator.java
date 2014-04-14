package com.luckia.biller.core.services.pdf;

import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

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
import com.luckia.biller.core.common.MathUtils;
import com.luckia.biller.core.model.Bill;
import com.luckia.biller.core.model.BillLiquidationDetail;
import com.luckia.biller.core.model.Liquidation;
import com.luckia.biller.core.services.bills.impl.BillDetailNameProvider;

public class PDFLiquidationGenerator extends PDFGenerator<Liquidation> {

	private static final Logger LOG = LoggerFactory.getLogger(PDFLiquidationGenerator.class);

	@Inject
	private BillDetailNameProvider billDetailNameProvider;

	private List<Map<String, String>> betDetails;
	private List<Map<String, String>> storeDetails;
	private List<Map<String, String>> satDetails;
	private List<Map<String, String>> otherDetails;
	private BigDecimal totalBetAmount;
	private BigDecimal totalStoreAmount;
	private BigDecimal totalSatAmount;
	private BigDecimal totalOtherAmount;

	@Override
	public void generate(Liquidation liquidation, OutputStream out) {
		LOG.debug("Generando PDF de la liquidacion {}", liquidation.getCode());
		try {
			init(liquidation);
			Rectangle rectangle = PageSize.A3;
			Document document = new Document(rectangle, 50f, 50f, 50f, 50f);
			PdfWriter writer = PdfWriter.getInstance(document, out);
			addMetaData(document, liquidation);
			document.open();
			addWaterMark(document, writer, liquidation);
			printLegalEntities(document, liquidation.getSender(), liquidation.getReceiver());
			printTitle(document, liquidation);
			printGeneralDetails(document, liquidation);

			// Desglose en conceptos
			printDetails(document, liquidation, "Honorarios por apuestas", betDetails, totalBetAmount);
			printDetails(document, liquidation, "Honorarios para bares", storeDetails, totalStoreAmount);
			printDetails(document, liquidation, "Honorarios SAT", satDetails, totalSatAmount);
			printDetails(document, liquidation, "Ajustes operativos", otherDetails, totalOtherAmount);

			document.close();
		} catch (Exception ex) {
			LOG.error("Error al generar el PDF de la liquidacion", ex);
		}
	}

	protected void init(Liquidation liquidation) {
		betDetails = new ArrayList<Map<String, String>>();
		storeDetails = new ArrayList<Map<String, String>>();
		satDetails = new ArrayList<Map<String, String>>();
		otherDetails = new ArrayList<Map<String, String>>();
		totalBetAmount = BigDecimal.ZERO;
		totalStoreAmount = BigDecimal.ZERO;
		totalSatAmount = BigDecimal.ZERO;
		totalOtherAmount = BigDecimal.ZERO;
		Map<String, String> map;
		for (Bill bill : liquidation.getBills()) {
			for (BillLiquidationDetail i : bill.getLiquidationDetails()) {
				String desc = billDetailNameProvider.getName(i);
				BigDecimal value = i.getValue();
				if (MathUtils.isNotZero(value)) {
					switch (i.getConcept()) {
					case GGR:
					case NGR:
					case NR:
					case Stakes:
						map = new HashMap<String, String>();
						map.put("name", bill.getSender().getName() + ": " + desc);
						map.put("value", formatAmount(value));
						betDetails.add(map);
						totalBetAmount = totalBetAmount.add(value);
						break;
					case SatMonthlyFees:
					case CommercialMonthlyFees:
						map = new HashMap<String, String>();
						map.put("name", bill.getSender().getName() + ": " + desc);
						map.put("value", formatAmount(value));
						satDetails.add(map);
						totalSatAmount = totalSatAmount.add(value);
						break;
					default:
						map = new HashMap<String, String>();
						map.put("name", bill.getSender().getName() + ": " + desc);
						map.put("value", formatAmount(value));
						otherDetails.add(map);
						totalOtherAmount.add(value);
						break;
					}
				}
			}
		}
	}

	private void printGeneralDetails(Document document, Liquidation liquidation) throws DocumentException {
		String senderName = liquidation.getSender().getName();
		String receiverName = liquidation.getReceiver().getName();
		PdfPTable table = new PdfPTable(new float[] { 30f, 10f, 10f, 10f, 10f });
		table.setWidthPercentage(100f);

		List<PdfPCell> cells = new ArrayList<PdfPCell>();

		cells.add(createCell("Liquidación", Element.ALIGN_LEFT, documentFont));
		cells.add(createCell(StringUtils.EMPTY, Element.ALIGN_RIGHT, documentFont));
		cells.add(createCell(StringUtils.EMPTY, Element.ALIGN_RIGHT, documentFont));
		cells.add(createCell(StringUtils.EMPTY, Element.ALIGN_RIGHT, documentFont));
		cells.add(createCell(StringUtils.EMPTY, Element.ALIGN_RIGHT, documentFont));

		cells.add(createCell("Descripción", Element.ALIGN_LEFT, boldFont));
		cells.add(createCell("Cantidad", Element.ALIGN_RIGHT, boldFont));
		cells.add(createCell("Precio unidad", Element.ALIGN_RIGHT, boldFont));
		cells.add(createCell("Desc. (%)", Element.ALIGN_RIGHT, boldFont));
		cells.add(createCell("Importe", Element.ALIGN_RIGHT, boldFont));

		if (MathUtils.isNotZero(totalBetAmount)) {
			cells.add(createCell("Honorarios por apuestas", Element.ALIGN_LEFT, documentFont));
			cells.add(createCell("1", Element.ALIGN_RIGHT, documentFont));
			cells.add(createCell(formatAmount(totalBetAmount), Element.ALIGN_RIGHT, documentFont));
			cells.add(createCell("-", Element.ALIGN_RIGHT, documentFont));
			cells.add(createCell(formatAmount(totalBetAmount), Element.ALIGN_RIGHT, documentFont));
		}

		if (MathUtils.isNotZero(totalStoreAmount)) {
			cells.add(createCell("Honorarios para bares", Element.ALIGN_LEFT, documentFont));
			cells.add(createCell("1", Element.ALIGN_RIGHT, documentFont));
			cells.add(createCell(formatAmount(totalStoreAmount), Element.ALIGN_RIGHT, documentFont));
			cells.add(createCell("-", Element.ALIGN_RIGHT, documentFont));
			cells.add(createCell(formatAmount(totalStoreAmount), Element.ALIGN_RIGHT, documentFont));
		}

		if (MathUtils.isNotZero(totalSatAmount)) {
			cells.add(createCell("Honorarios SAT", Element.ALIGN_LEFT, documentFont));
			cells.add(createCell("1", Element.ALIGN_RIGHT, documentFont));
			cells.add(createCell(formatAmount(totalSatAmount), Element.ALIGN_RIGHT, documentFont));
			cells.add(createCell("-", Element.ALIGN_RIGHT, documentFont));
			cells.add(createCell(formatAmount(totalSatAmount), Element.ALIGN_RIGHT, documentFont));
		}

		if (MathUtils.isNotZero(totalOtherAmount)) {
			cells.add(createCell("Ajustes operativos", Element.ALIGN_LEFT, documentFont));
			cells.add(createCell("1", Element.ALIGN_RIGHT, documentFont));
			cells.add(createCell(formatAmount(totalOtherAmount), Element.ALIGN_RIGHT, documentFont));
			cells.add(createCell("-", Element.ALIGN_RIGHT, documentFont));
			cells.add(createCell(formatAmount(totalOtherAmount), Element.ALIGN_RIGHT, documentFont));
		}
		cells.add(createCell("TOTAL LIQUIDACIÓN", Element.ALIGN_LEFT, boldFont));
		cells.addAll(createEmptyCells(3));
		cells.add(createCell(formatAmount(liquidation.getAmount()), Element.ALIGN_RIGHT, boldFont));

		cells.addAll(createEmptyCells(5));

		cells.add(createCell("Recaudación en posesión de " + senderName, Element.ALIGN_LEFT, documentFont));
		cells.addAll(createEmptyCells(3));
		cells.add(createCell(formatAmount(BigDecimal.ZERO), Element.ALIGN_RIGHT, documentFont));

		cells.add(createCell("Ajustes operativos (100%)", Element.ALIGN_LEFT, documentFont));
		cells.addAll(createEmptyCells(3));
		cells.add(createCell(formatAmount(BigDecimal.ZERO), Element.ALIGN_RIGHT, documentFont));

		cells.add(createCell("Recaudación en posesión de " + senderName, Element.ALIGN_LEFT, documentFont));
		cells.addAll(createEmptyCells(3));
		cells.add(createCell(formatAmount(BigDecimal.ZERO), Element.ALIGN_RIGHT, documentFont));

		cells.add(createCell("Total liquidación a percibir por " + senderName, Element.ALIGN_LEFT, documentFont));
		cells.addAll(createEmptyCells(3));
		cells.add(createCell(formatAmount(BigDecimal.ZERO), Element.ALIGN_RIGHT, documentFont));

		String message;
		if (StringUtils.isBlank(liquidation.getReceiver().getAccountNumber())) {
			message = String.format("Total a ingresar a %s", senderName);
		} else {
			message = String.format("Total a ingresar a %s (%s)", receiverName, liquidation.getReceiver().getAccountNumber());
		}
		cells.add(createCell(message, Element.ALIGN_LEFT, boldFont));
		cells.addAll(createEmptyCells(3));
		cells.add(createCell(formatAmount(liquidation.getAmount()), Element.ALIGN_RIGHT, boldFont));

		int cols = 5;
		for (int i = 0; i < cells.size(); i++) {
			int col = i % cols;
			int row = i / cols;
			PdfPCell cell = cells.get(i);
			cell.setPaddingLeft(8f);
			cell.setPaddingRight(8f);
			cell.setPaddingBottom(3f);
			cell.setPaddingTop(3f);

			// cell.setPadding(8f);
			if (col == 0) {
				cell.setBorderWidthLeft(2f);
			} else if (col == 4) {
				cell.setBorderWidthRight(2f);
			}
			if (row == 0) {
				cell.setBorderWidthTop(2f);
			} else if (row == 1) {
				cell.setBorderWidthBottom(1f);
				cell.setPaddingBottom(8f);
			} else if (row == (cells.size() / cols) - 8) { // linea antes de mostrar el total
				cell.setPaddingBottom(8f);
				cell.setBorderWidthBottom(1f);
			} else if (row == (cells.size() / cols) - 1) {
				cell.setPaddingBottom(8f);
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

	protected void printDetails(Document document, Liquidation liquidation, String title, List<Map<String, String>> details, BigDecimal totalAmount) throws DocumentException {
		if (details.isEmpty()) {
			return;
		}
		document.newPage();
		PdfPTable table = new PdfPTable(new float[] { 30f, 10f, 10f, 10f, 10f });
		table.setWidthPercentage(100f);

		List<PdfPCell> cells = new ArrayList<PdfPCell>();

		cells.add(createCell(title, Element.ALIGN_LEFT, documentFont));
		cells.addAll(createEmptyCells(4));

		cells.add(createCell("Local", Element.ALIGN_LEFT, boldFont));
		cells.add(createCell("Cantidad", Element.ALIGN_RIGHT, boldFont));
		cells.add(createCell("Precio unidad", Element.ALIGN_RIGHT, boldFont));
		cells.add(createCell("Desc. (%)", Element.ALIGN_RIGHT, boldFont));
		cells.add(createCell("Importe", Element.ALIGN_RIGHT, boldFont));

		for (Map<String, String> map : details) {
			cells.add(createCell(map.get("name"), Element.ALIGN_LEFT, documentFont));
			cells.add(createCell("1", Element.ALIGN_RIGHT, documentFont));
			cells.add(createCell("", Element.ALIGN_RIGHT, documentFont));
			cells.add(createCell("", Element.ALIGN_RIGHT, documentFont));
			cells.add(createCell(map.get("value"), Element.ALIGN_RIGHT, documentFont));
		}

		cells.add(createCell("TOTAL", Element.ALIGN_LEFT, boldFont));
		cells.addAll(createEmptyCells(3));
		cells.add(createCell(formatAmount(totalAmount), Element.ALIGN_RIGHT, boldFont));

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
				// } else if (row == (cells.size() / cols) - 8) { // linea antes de mostrar el total
				// cell.setBorderWidthBottom(1f);
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
