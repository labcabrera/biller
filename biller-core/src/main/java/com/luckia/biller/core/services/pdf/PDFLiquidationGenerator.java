package com.luckia.biller.core.services.pdf;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.luckia.biller.core.common.MathUtils;
import com.luckia.biller.core.model.BillConcept;
import com.luckia.biller.core.model.Liquidation;
import com.luckia.biller.core.model.LiquidationDetail;

public class PDFLiquidationGenerator extends PDFGenerator<Liquidation> {

	private static final Logger LOG = LoggerFactory.getLogger(PDFLiquidationGenerator.class);

	@Inject
	private PDFLiquidationDetailProcessor detailProcessor;

	private Map<BillConcept, PDFLiquidationDetail> details;
	private Map<BillConcept, PDFLiquidationDetail> outerDetails;

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
			document.close();
		} catch (Exception ex) {
			LOG.error("Error al generar el PDF de la liquidacion", ex);
		}
	}

	protected void init(Liquidation liquidation) {
		details = detailProcessor.loadDetails(liquidation);
		outerDetails = detailProcessor.loadOuterDetails(liquidation);
	}

	private void printGeneralDetails(Document document, Liquidation liquidation) throws DocumentException {
		String senderName = liquidation.getSender().getName();
		String receiverName = liquidation.getReceiver().getName();
		PdfPTable table = new PdfPTable(new float[] { 30f, 10f, 10f, 10f, 10f, 10f });
		table.setWidthPercentage(100f);

		List<PdfPCell> cells = new ArrayList<>();

		cells.add(createCell("Liquidación", Element.ALIGN_LEFT, documentFont));
		cells.add(createCell(StringUtils.EMPTY, Element.ALIGN_RIGHT, documentFont));
		cells.add(createCell(StringUtils.EMPTY, Element.ALIGN_RIGHT, documentFont));
		cells.add(createCell(StringUtils.EMPTY, Element.ALIGN_RIGHT, documentFont));
		cells.add(createCell(StringUtils.EMPTY, Element.ALIGN_RIGHT, documentFont));
		cells.add(createCell(StringUtils.EMPTY, Element.ALIGN_RIGHT, documentFont));

		cells.add(createCell("Descripción", Element.ALIGN_LEFT, boldFont));
		cells.add(createCell("Cantidad", Element.ALIGN_RIGHT, boldFont));
		cells.add(createCell("Precio unidad", Element.ALIGN_RIGHT, boldFont));
		cells.add(createCell("Base imponible", Element.ALIGN_RIGHT, boldFont));
		cells.add(createCell("IVA / EGIC", Element.ALIGN_RIGHT, boldFont));
		cells.add(createCell("Importe", Element.ALIGN_RIGHT, boldFont));

		for (Entry<BillConcept, PDFLiquidationDetail> entry : details.entrySet()) {
			if (MathUtils.isNotZero(entry.getValue().getAmount())) {
				cells.add(createCell("" + entry.getValue().getName(), Element.ALIGN_LEFT, documentFont));
				cells.add(createCell("1", Element.ALIGN_RIGHT, documentFont));
				cells.add(createCell(formatAmount(entry.getValue().getNetAmount()), Element.ALIGN_RIGHT, documentFont));
				cells.add(createCell(formatAmount(entry.getValue().getNetAmount()), Element.ALIGN_RIGHT, documentFont));
				cells.add(createCell(formatAmount(entry.getValue().getVatAmount()), Element.ALIGN_RIGHT, documentFont));
				cells.add(createCell(formatAmount(entry.getValue().getAmount()), Element.ALIGN_RIGHT, documentFont));
			}
		}

		if (liquidation.getDetails() != null) {
			for (LiquidationDetail detail : liquidation.getDetails()) {
				if (detail.getLiquidationIncluded()) {
					cells.add(createCell(detail.getName(), Element.ALIGN_LEFT, documentFont));
					cells.add(createCell("1", Element.ALIGN_RIGHT, documentFont));
					cells.add(createCell(formatAmount(detail.getNetValue()), Element.ALIGN_RIGHT, documentFont));
					cells.add(createCell(formatAmount(detail.getNetValue()), Element.ALIGN_RIGHT, documentFont));
					cells.add(createCell(formatAmount(detail.getVatValue()), Element.ALIGN_RIGHT, documentFont));
					cells.add(createCell(formatAmount(detail.getValue()), Element.ALIGN_RIGHT, documentFont));
				}
			}
		}

		cells.add(createCell("TOTAL LIQUIDACIÓN", Element.ALIGN_LEFT, boldFont));
		cells.addAll(createEmptyCells(4));
		cells.add(createCell(formatAmount(liquidation.getLiquidationResults().getTotalAmount()), Element.ALIGN_RIGHT, boldFont));

		cells.addAll(createEmptyCells(6));

		cells.add(createCell("Saldo de caja de " + senderName, Element.ALIGN_LEFT, boldFont));
		cells.addAll(createEmptyCells(4));
		cells.add(createCell(formatAmount(liquidation.getLiquidationResults().getCashStoreAmount()), Element.ALIGN_RIGHT, boldFont));

		// if (MathUtils.isNotZero(liquidation.getLiquidationResults().getStoreManualOuterAmount())) {
		// cells.add(createCell("Ajustes de los establecimientos", Element.ALIGN_LEFT, documentFont));
		// cells.addAll(createEmptyCells(4));
		// cells.add(createCell(formatAmount(liquidation.getLiquidationResults().getStoreManualOuterAmount()), Element.ALIGN_RIGHT, documentFont));
		// }

		// Ajustes manuales externos a la liquidacion
		for (Entry<BillConcept, PDFLiquidationDetail> entry : outerDetails.entrySet()) {
			if (MathUtils.isNotZero(entry.getValue().getAmount())) {
				cells.add(createCell("" + entry.getValue().getName(), Element.ALIGN_LEFT, documentFont));
				cells.add(createCell("", Element.ALIGN_RIGHT, documentFont));
				cells.add(createCell("", Element.ALIGN_RIGHT, documentFont));
				cells.add(createCell("", Element.ALIGN_RIGHT, documentFont));
				cells.add(createCell("", Element.ALIGN_RIGHT, documentFont));
				cells.add(createCell(formatAmount(entry.getValue().getAmount()), Element.ALIGN_RIGHT, documentFont));
			}
		}

		cells.add(createCell("Saldo de caja ajustado", Element.ALIGN_LEFT, boldFont));
		cells.addAll(createEmptyCells(4));
		cells.add(createCell(formatAmount(liquidation.getLiquidationResults().getCashStoreAdjustmentAmount()), Element.ALIGN_RIGHT, boldFont));

		cells.add(createCell("Total liquidación a percibir por " + senderName, Element.ALIGN_LEFT, documentFont));
		cells.addAll(createEmptyCells(4));
		cells.add(createCell(formatAmount(liquidation.getLiquidationResults().getSenderAmount()), Element.ALIGN_RIGHT, documentFont));

		String message;
		if (StringUtils.isBlank(liquidation.getReceiver().getAccountNumber())) {
			message = String.format("Total a ingresar a %s", receiverName);
		} else {
			message = String.format("Total a ingresar a %s (%s)", receiverName, liquidation.getReceiver().getAccountNumber());
		}
		cells.add(createCell(message, Element.ALIGN_LEFT, boldFont));
		cells.addAll(createEmptyCells(4));
		cells.add(createCell(formatAmount(liquidation.getLiquidationResults().getEffectiveLiquidationAmount()), Element.ALIGN_RIGHT, boldFont));

		int cols = 6;
		int subtotalRow = 2 + details.size();
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
			} else if (col == cols - 1) {
				cell.setBorderWidthRight(2f);
			}
			if (row == 0) {
				cell.setBorderWidthTop(2f);
			} else if (row == 1) {
				cell.setBorderWidthBottom(1f);
				cell.setPaddingBottom(8f);
			} else if (row == subtotalRow) {
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

	@Override
	protected float getLineHeight() {
		return 20;
	}
}
