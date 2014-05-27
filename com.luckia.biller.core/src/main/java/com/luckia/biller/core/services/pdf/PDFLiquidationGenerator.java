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
import com.luckia.biller.core.model.BillDetail;
import com.luckia.biller.core.model.BillLiquidationDetail;
import com.luckia.biller.core.model.Liquidation;
import com.luckia.biller.core.model.LiquidationDetail;
import com.luckia.biller.core.services.bills.impl.BillDetailNameProvider;

public class PDFLiquidationGenerator extends PDFGenerator<Liquidation> {

	private static final Logger LOG = LoggerFactory.getLogger(PDFLiquidationGenerator.class);

	@Inject
	private BillDetailNameProvider billDetailNameProvider;

	private List<Map<String, Object>> betDetails;
	private List<Map<String, Object>> storeDetails;
	private List<Map<String, Object>> satDetails;
	private List<Map<String, Object>> adjustmentDetails;
	private List<Map<String, Object>> otherDetails;

	@Override
	public void generate(Liquidation liquidation, OutputStream out) {
		LOG.debug("Generando PDF de la liquidacion");
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
			printCommentsPdf(document, liquidation);

			// Desglose en conceptos
			printDetails("Local", document, liquidation, "Honorarios por apuestas", betDetails);
			printDetails("Local", document, liquidation, "Honorarios para bares", storeDetails);
			printDetails("Local", document, liquidation, "Honorarios SAT", satDetails);
			printDetails("Local", document, liquidation, "Ajustes operativos", adjustmentDetails);
			printDetails("Concepto", document, liquidation, "Otros", otherDetails);

			document.close();
		} catch (Exception ex) {
			LOG.error("Error al generar el PDF de la liquidacion", ex);
		}
	}

	protected void init(Liquidation liquidation) {
		betDetails = new ArrayList<>();
		storeDetails = new ArrayList<>();
		satDetails = new ArrayList<>();
		adjustmentDetails = new ArrayList<>();
		otherDetails = new ArrayList<>();
		Map<String, Object> map;
		for (LiquidationDetail detail : liquidation.getDetails()) {
			map = new HashMap<>();
			map.put("name", detail.getName());
			map.put("value", detail.getValue());
			otherDetails.add(map);
			break;
		}
		for (Bill bill : liquidation.getBills()) {
			// En primer lugar computamos los detalles de liquidacion de la factura
			LOG.debug("Inspeccionando detalles de la factura {}", bill.getSender().getName());
			for (BillLiquidationDetail i : bill.getLiquidationDetails()) {
				String desc = billDetailNameProvider.getName(i);
				BigDecimal value = i.getValue();
				if (MathUtils.isNotZero(value) && i.getConcept() != null) {
					switch (i.getConcept()) {
					case GGR:
					case NGR:
					case NR:
					case Stakes:
						LOG.debug("Honorario por apuestas: {} ({})", i.getConcept(), value);
						map = new HashMap<>();
						map.put("name", bill.getSender().getName() + ": " + desc);
						map.put("value", value);
						betDetails.add(map);
						break;
					case SatMonthlyFees:
					case CommercialMonthlyFees:
						LOG.debug("Honorario SAT: {} ({})", i.getConcept(), value);
						map = new HashMap<>();
						map.put("name", bill.getSender().getName() + ": " + desc);
						map.put("value", value);
						satDetails.add(map);
						break;
					default:
						LOG.debug("Ignorando concepto {}", i.getConcept());
						break;
					}
				}
			}
			// En segundo lugar computamos los detalles de la factura que aplican a la liquidacion (ajustes operativos y ajustes manuales)
			for (BillDetail detail : bill.getDetails()) {
				if (detail.getConcept() != null) {
					switch (detail.getConcept()) {
					case Adjustment:
						map = new HashMap<>();
						map.put("name", bill.getSender().getName() + ": " + detail.getName());
						map.put("value", detail.getValue());
						adjustmentDetails.add(map);
						break;
					case ManualWithLiquidation:
						map = new HashMap<>();
						map.put("name", bill.getSender().getName() + ": " + detail.getName());
						map.put("value", detail.getValue());
						otherDetails.add(map);
						break;
					default:
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

		List<PdfPCell> cells = new ArrayList<>();

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

		if (MathUtils.isNotZero(liquidation.getLiquidationResults().getBetAmount())) {
			cells.add(createCell("Honorarios por apuestas", Element.ALIGN_LEFT, documentFont));
			cells.add(createCell("1", Element.ALIGN_RIGHT, documentFont));
			cells.add(createCell(formatAmount(liquidation.getLiquidationResults().getBetAmount()), Element.ALIGN_RIGHT, documentFont));
			cells.add(createCell("-", Element.ALIGN_RIGHT, documentFont));
			cells.add(createCell(formatAmount(liquidation.getLiquidationResults().getBetAmount()), Element.ALIGN_RIGHT, documentFont));
		}

		if (MathUtils.isNotZero(liquidation.getLiquidationResults().getStoreAmount())) {
			cells.add(createCell("Honorarios para bares", Element.ALIGN_LEFT, documentFont));
			cells.add(createCell("1", Element.ALIGN_RIGHT, documentFont));
			cells.add(createCell(formatAmount(liquidation.getLiquidationResults().getStoreAmount()), Element.ALIGN_RIGHT, documentFont));
			cells.add(createCell("-", Element.ALIGN_RIGHT, documentFont));
			cells.add(createCell(formatAmount(liquidation.getLiquidationResults().getStoreAmount()), Element.ALIGN_RIGHT, documentFont));
		}

		if (MathUtils.isNotZero(liquidation.getLiquidationResults().getSatAmount())) {
			cells.add(createCell("Honorarios SAT", Element.ALIGN_LEFT, documentFont));
			cells.add(createCell("1", Element.ALIGN_RIGHT, documentFont));
			cells.add(createCell(formatAmount(liquidation.getLiquidationResults().getSatAmount()), Element.ALIGN_RIGHT, documentFont));
			cells.add(createCell("-", Element.ALIGN_RIGHT, documentFont));
			cells.add(createCell(formatAmount(liquidation.getLiquidationResults().getSatAmount()), Element.ALIGN_RIGHT, documentFont));
		}

		if (MathUtils.isNotZero(liquidation.getLiquidationResults().getAdjustmentSharedAmount())) {
			cells.add(createCell("Ajustes operativos", Element.ALIGN_LEFT, documentFont));
			cells.add(createCell("1", Element.ALIGN_RIGHT, documentFont));
			cells.add(createCell(formatAmount(liquidation.getLiquidationResults().getAdjustmentAmount()), Element.ALIGN_RIGHT, documentFont));
			cells.add(createCell("50%", Element.ALIGN_RIGHT, documentFont));
			cells.add(createCell(formatAmount(liquidation.getLiquidationResults().getAdjustmentSharedAmount()), Element.ALIGN_RIGHT, documentFont));
		}

		if (liquidation.getDetails() != null) {
			for (LiquidationDetail detail : liquidation.getDetails()) {
				cells.add(createCell(detail.getName(), Element.ALIGN_LEFT, documentFont));
				cells.add(createCell("1", Element.ALIGN_RIGHT, documentFont));
				cells.add(createCell(formatAmount(detail.getValue()), Element.ALIGN_RIGHT, documentFont));
				cells.add(createCell("-", Element.ALIGN_RIGHT, documentFont));
				cells.add(createCell(formatAmount(detail.getValue()), Element.ALIGN_RIGHT, documentFont));
			}
		}

		if (MathUtils.isNotZero(liquidation.getLiquidationResults().getOtherAmount())) {
			cells.add(createCell("Otros conceptos", Element.ALIGN_LEFT, documentFont));
			cells.add(createCell("1", Element.ALIGN_RIGHT, documentFont));
			cells.add(createCell(formatAmount(liquidation.getLiquidationResults().getOtherAmount()), Element.ALIGN_RIGHT, documentFont));
			cells.add(createCell("-", Element.ALIGN_RIGHT, documentFont));
			cells.add(createCell(formatAmount(liquidation.getLiquidationResults().getOtherAmount()), Element.ALIGN_RIGHT, documentFont));
		}

		cells.add(createCell("TOTAL LIQUIDACIÓN", Element.ALIGN_LEFT, boldFont));
		cells.addAll(createEmptyCells(3));
		cells.add(createCell(formatAmount(liquidation.getLiquidationResults().getSenderAmount()), Element.ALIGN_RIGHT, boldFont));

		cells.addAll(createEmptyCells(5));

		cells.add(createCell("Saldo de caja de " + senderName, Element.ALIGN_LEFT, documentFont));
		cells.addAll(createEmptyCells(3));
		cells.add(createCell(formatAmount(liquidation.getLiquidationResults().getCashStoreAmount()), Element.ALIGN_RIGHT, documentFont));

		cells.add(createCell("Ajustes operativos (100%)", Element.ALIGN_LEFT, documentFont));
		cells.addAll(createEmptyCells(3));
		cells.add(createCell(formatAmount(liquidation.getLiquidationResults().getAdjustmentAmount()), Element.ALIGN_RIGHT, documentFont));

		cells.add(createCell("Recaudación en posesión de " + senderName, Element.ALIGN_LEFT, documentFont));
		cells.addAll(createEmptyCells(3));
		cells.add(createCell(formatAmount(liquidation.getLiquidationResults().getCashStoreAdjustmentAmount()), Element.ALIGN_RIGHT, documentFont));

		cells.add(createCell("Total liquidación a percibir por " + senderName, Element.ALIGN_LEFT, documentFont));
		cells.addAll(createEmptyCells(3));
		cells.add(createCell(formatAmount(liquidation.getLiquidationResults().getSenderAmount()), Element.ALIGN_RIGHT, documentFont));

		String message;
		if (StringUtils.isBlank(liquidation.getReceiver().getAccountNumber())) {
			message = String.format("Total a ingresar a %s", senderName);
		} else {
			message = String.format("Total a ingresar a %s (%s)", receiverName, liquidation.getReceiver().getAccountNumber());
		}
		cells.add(createCell(message, Element.ALIGN_LEFT, boldFont));
		cells.addAll(createEmptyCells(3));
		cells.add(createCell(formatAmount(liquidation.getLiquidationResults().getReceiverAmount()), Element.ALIGN_RIGHT, boldFont));

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

	protected void printDetails(String title, Document document, Liquidation liquidation, String conceptName, List<Map<String, Object>> details) throws DocumentException {
		if (details.isEmpty()) {
			return;
		}
		document.newPage();
		PdfPTable table = new PdfPTable(new float[] { 30f, 10f, 10f, 10f, 10f });
		table.setWidthPercentage(100f);

		List<PdfPCell> cells = new ArrayList<PdfPCell>();

		cells.add(createCell(title, Element.ALIGN_LEFT, boldFont));
		cells.addAll(createEmptyCells(4));

		cells.add(createCell(conceptName, Element.ALIGN_LEFT, boldFont));
		cells.add(createCell("Cantidad", Element.ALIGN_RIGHT, boldFont));
		cells.add(createCell("Precio unidad", Element.ALIGN_RIGHT, boldFont));
		cells.add(createCell("Desc. (%)", Element.ALIGN_RIGHT, boldFont));
		cells.add(createCell("Importe", Element.ALIGN_RIGHT, boldFont));

		BigDecimal total = BigDecimal.ZERO;
		for (Map<String, Object> map : details) {
			BigDecimal partial = (BigDecimal) map.get("value");
			cells.add(createCell((String) map.get("name"), Element.ALIGN_LEFT, documentFont));
			cells.add(createCell("1", Element.ALIGN_RIGHT, documentFont));
			cells.add(createCell(formatAmount(partial), Element.ALIGN_RIGHT, documentFont));
			cells.add(createCell("-", Element.ALIGN_RIGHT, documentFont));
			cells.add(createCell(formatAmount(partial), Element.ALIGN_RIGHT, documentFont));
			total = total.add(partial);
		}

		cells.add(createCell("TOTAL", Element.ALIGN_LEFT, boldFont));
		cells.addAll(createEmptyCells(3));
		cells.add(createCell(formatAmount(total), Element.ALIGN_RIGHT, boldFont));

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
