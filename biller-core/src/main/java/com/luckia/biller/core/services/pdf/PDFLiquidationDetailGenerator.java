package com.luckia.biller.core.services.pdf;

import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.luckia.biller.core.model.AbstractBill;
import com.luckia.biller.core.model.Bill;
import com.luckia.biller.core.model.BillLiquidationDetail;
import com.luckia.biller.core.model.VatLiquidationType;
import com.luckia.biller.core.services.entities.ProvinceTaxesService;

public class PDFLiquidationDetailGenerator extends PDFGenerator<Bill> {

	private static final Logger LOG = LoggerFactory.getLogger(PDFLiquidationDetailGenerator.class);

	@Inject
	private ProvinceTaxesService provinceTaxesService;

	@Override
	public void generate(Bill bill, OutputStream out) {
		LOG.debug("Generando PDF de la liquidacion");
		try {
			Rectangle rectangle = PageSize.A3;
			Document document = new Document(rectangle, 50f, 50f, 50f, 50f);
			PdfWriter writer = PdfWriter.getInstance(document, out);
			addMetaData(document, bill);
			document.open();
			addWaterMark(document, writer, bill);
			printLegalEntities(document, bill.getReceiver(), bill.getModel().getReceiver());
			printTitle(document, bill);
			printGeneralDetails(document, bill);
			printCommentsPdf(document, bill);
			document.close();
		} catch (Exception ex) {
			LOG.error("Error al generar el PDF de la liquidacion", ex);
		}
	}

	private void printGeneralDetails(Document document, Bill bill) throws DocumentException {
		String senderName = bill.getSender().getName();
		// String receiverName = liquidation.getReceiver().getName();
		boolean hasVat = bill.getLiquidation().vatApplies(bill.getLiquidation().getModel());
		BigDecimal vatPercent = provinceTaxesService.getVatPercent(bill);
		PdfPTable table;
		if (hasVat) {
			table = new PdfPTable(new float[] { 50f, 10f, 10f, 10f, 10f, 10f, 10f });
		} else {
			table = new PdfPTable(new float[] { 60f, 10f, 10f, 10f, 10f });
		}
		table.setWidthPercentage(100f);
		List<PdfPCell> cells = new ArrayList<>();

		// Header
		cells.add(createCell("Liquidación", Element.ALIGN_LEFT, documentFont));
		cells.addAll(createEmptyCells(hasVat ? 6 : 4));

		// Titles
		cells.add(createCell("Descripción", Element.ALIGN_LEFT, boldFont));
		cells.add(createCell("Cantidad", Element.ALIGN_RIGHT, boldFont));
		cells.add(createCell("Precio unidad", Element.ALIGN_RIGHT, boldFont));
		cells.add(createCell("Base imponible", Element.ALIGN_RIGHT, boldFont));
		if (hasVat) {
			cells.add(createCell("IVA/IGIC", Element.ALIGN_RIGHT, boldFont));
			cells.add(createCell("IVA/IGIC", Element.ALIGN_RIGHT, boldFont));
		}
		cells.add(createCell("Importe", Element.ALIGN_RIGHT, boldFont));

		// Main concepts
		for (BillLiquidationDetail detail : bill.getLiquidationDetails()) {
			if (detail.getLiquidationIncluded()) {
				cells.add(createCell("" + detail.getName(), Element.ALIGN_LEFT, documentFont));
				cells.add(createCell("1", Element.ALIGN_RIGHT, documentFont));
				cells.add(createCell(formatAmount(detail.getNetValue()), Element.ALIGN_RIGHT, documentFont));
				cells.add(createCell(formatAmount(detail.getNetValue()), Element.ALIGN_RIGHT, documentFont));
				if (hasVat) {
					cells.add(createCell(formatAmount(vatPercent, false) + "%", Element.ALIGN_RIGHT, documentFont));
					cells.add(createCell(formatAmount(detail.getVatValue()), Element.ALIGN_RIGHT, documentFont));
				}
				cells.add(createCell(formatAmount(detail.getValue()), Element.ALIGN_RIGHT, documentFont));
			}
		}

		cells.add(createCell("TOTAL LIQUIDACIÓN", Element.ALIGN_LEFT, boldFont));
		if (hasVat) {
			cells.addAll(createEmptyCells(2));
			cells.add(createCell(formatAmount(bill.getLiquidationTotalNetAmount()), Element.ALIGN_RIGHT, boldFont));
			cells.add(createCell(formatAmount(vatPercent, false) + "%", Element.ALIGN_RIGHT, boldFont));
			cells.add(createCell(formatAmount(bill.getLiquidationTotalVat()), Element.ALIGN_RIGHT, boldFont));
		} else {
			cells.addAll(createEmptyCells(3));
		}
		cells.add(createCell(formatAmount(bill.getLiquidationTotalAmount()), Element.ALIGN_RIGHT, boldFont));

		cells.addAll(createEmptyCells(hasVat ? 7 : 5));

		cells.add(createCell("Saldo de caja de " + senderName, Element.ALIGN_LEFT, boldFont));
		cells.addAll(createEmptyCells(hasVat ? 5 : 3));
		cells.add(createCell(formatAmount(bill.getStoreCash()), Element.ALIGN_RIGHT, boldFont));

		// Main concepts
		for (BillLiquidationDetail detail : bill.getLiquidationDetails()) {
			if (!detail.getLiquidationIncluded()) {
				cells.add(createCell("" + detail.getName(), Element.ALIGN_LEFT, documentFont));
				cells.add(createCell("", Element.ALIGN_RIGHT, documentFont));
				cells.add(createCell("", Element.ALIGN_RIGHT, documentFont));
				cells.add(createCell("", Element.ALIGN_RIGHT, documentFont));
				cells.add(createCell(formatAmount(detail.getValue()), Element.ALIGN_RIGHT, documentFont));
			}
		}

		int cols = hasVat ? 7 : 5;
		int subtotalRow = 2 + bill.getLiquidationDetails().size();
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

	@Override
	protected String getPdfTitle(AbstractBill abstractBill) {
		// Dependiendo de si es una co-explotacion o una factura mostramos un titulo u otro
		String billTitleLabel;
		Bill liquidation = (Bill) abstractBill;
		VatLiquidationType vatType = VatLiquidationType.EXCLUDED;
		if (liquidation.getModel() != null && liquidation.getModel().getVatLiquidationType() != null) {
			vatType = liquidation.getModel().getVatLiquidationType();
		}
		switch (vatType) {
		case EXCLUDED:
			billTitleLabel = i18nService.getMessage("pdf.label.liquidationTitle");
			break;
		default:
			billTitleLabel = i18nService.getMessage("pdf.label.liquidationTitleWithVat");
			break;
		}

		return billTitleLabel;
	}
}
