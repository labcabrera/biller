package com.luckia.biller.core.reporting;

import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.luckia.biller.core.model.Bill;
import com.luckia.biller.core.model.BillConcept;
import com.luckia.biller.core.model.BillLiquidationDetail;
import com.luckia.biller.core.model.LegalEntity;
import com.luckia.biller.core.model.Liquidation;
import com.luckia.biller.core.model.common.Message;

/**
 * Componente encargado de generar los reportes de liquidaciones.
 */
public class LiquidationReportGenerator extends BaseReport {

	private static final Logger LOG = LoggerFactory.getLogger(LiquidationReportGenerator.class);

	private final LiquidationReportDataSource dataSource;

	@Inject
	public LiquidationReportGenerator(LiquidationReportDataSource dataSource) {
		this.dataSource = dataSource;
	}

	public Message<String> generate(Date from, Date to, List<LegalEntity> entities) {
		try {
			Map<LegalEntity, List<Liquidation>> liquidationMap = dataSource.getLiquidations(from, to, entities);
			if (!liquidationMap.isEmpty()) {
				FileOutputStream fileOut = new FileOutputStream("./target/liquidations.xls");
				HSSFWorkbook workbook = new HSSFWorkbook();
				for (LegalEntity legalEntity : liquidationMap.keySet()) {
					List<Liquidation> liquidations = liquidationMap.get(legalEntity);
					processSheet(legalEntity, liquidations, workbook);
				}
				workbook.write(fileOut);
				fileOut.flush();
				fileOut.close();
				return new Message<>(Message.CODE_SUCCESS, String.format("Generado report de %s liquidaciones", liquidationMap.size()));
			} else {
				return new Message<>(Message.CODE_SUCCESS, "No se han encontrado liquidaciones");
			}
		} catch (Exception ex) {
			LOG.error("Error al generar el report de liquidaciones", ex);
			return new Message<>(Message.CODE_GENERIC_ERROR, "Error al generar el report de liquidaciones");
		}
	}

	private void processSheet(LegalEntity operator, List<Liquidation> liquidations, HSSFWorkbook workbook) {
		HSSFSheet sheet = workbook.createSheet(operator.getName());
		int currentRow = 0;
		for (int i = 0; i < liquidations.size(); i++) {
			Liquidation liquidation = liquidations.get(i);
			currentRow = createHeader(sheet, currentRow++);
			currentRow = createLiquidationDetails(sheet, currentRow, liquidation);
			currentRow = createFooter(sheet, currentRow, liquidation);
			currentRow += 3;
		}
		for (int i = 0; i < 18; i++) {
			sheet.autoSizeColumn(i);
		}
	}

	private int createHeader(HSSFSheet sheet, int currentRow) {
		int cell = 0;
		createHeaderCell(sheet, currentRow, cell++, "Operadora");
		createHeaderCell(sheet, currentRow, cell++, "Local");
		createHeaderCell(sheet, currentRow, cell++, "Fecha");
		createHeaderCell(sheet, currentRow, cell++, "Estado");
		createHeaderCell(sheet, currentRow, cell++, "Factura");
		createHeaderCell(sheet, currentRow, cell++, "Liquidacion");
		createHeaderCell(sheet, currentRow, cell++, "Desde");
		createHeaderCell(sheet, currentRow, cell++, "Hasta");
		createHeaderCell(sheet, currentRow, cell++, "Saldo de caja");
		createHeaderCell(sheet, currentRow, cell++, "Modelo");
		cell++;
		createHeaderCell(sheet, currentRow, cell++, "Importe neto");
		createHeaderCell(sheet, currentRow, cell++, "Iva");
		cell++;
		createHeaderCell(sheet, currentRow, cell++, "Stakes (base)");
		createHeaderCell(sheet, currentRow, cell++, "GGR (base)");
		createHeaderCell(sheet, currentRow, cell++, "NGR (base)");
		createHeaderCell(sheet, currentRow, cell++, "NR (base)");
		cell++;
		createHeaderCell(sheet, currentRow, cell++, "Stakes");
		createHeaderCell(sheet, currentRow, cell++, "GGR");
		createHeaderCell(sheet, currentRow, cell++, "NGR");
		createHeaderCell(sheet, currentRow, cell++, "NR");
		return currentRow + 1;
	}

	private int createFooter(HSSFSheet sheet, int currentRow, Liquidation liquidation) {
		int textCol = 3;
		int valueCol = 5;
		createCell(sheet, currentRow, textCol, "Total:");
		createCell(sheet, currentRow, valueCol, liquidation.getLiquidationResults().getSenderAmount());
		currentRow++;
		createCell(sheet, currentRow, textCol, "Ajustes:");
		createCell(sheet, currentRow, valueCol, liquidation.getLiquidationResults().getAdjustmentAmount());
		currentRow++;
		createCell(sheet, currentRow, textCol, "Saldo de caja:");
		createCell(sheet, currentRow, valueCol, liquidation.getLiquidationResults().getCashStoreAmount());
		currentRow++;
		createCell(sheet, currentRow, textCol, "Resultado:");
		createCell(sheet, currentRow, valueCol, liquidation.getLiquidationResults().getReceiverAmount());
		return currentRow + 1;

	}

	private int createLiquidationDetails(HSSFSheet sheet, int currentRow, Liquidation liquidation) {
		for (int i = 0; i < liquidation.getBills().size(); i++) {
			Bill bill = liquidation.getBills().get(i);
			int cell = 0;
			createCell(sheet, currentRow, cell++, liquidation.getSender().getName());
			createCell(sheet, currentRow, cell++, bill.getSender().getName());
			createCell(sheet, currentRow, cell++, bill.getBillDate());
			createCell(sheet, currentRow, cell++, bill.getCurrentState().getStateDefinition().getDesc());
			createCell(sheet, currentRow, cell++, bill.getAmount());
			createCell(sheet, currentRow, cell++, bill.getLiquidationTotalAmount());
			createCell(sheet, currentRow, cell++, bill.getDateFrom());
			createCell(sheet, currentRow, cell++, bill.getDateTo());
			createCell(sheet, currentRow, cell++, bill.getStoreCash());
			createCell(sheet, currentRow, cell++, bill.getModel().getName());
			cell++;
			createCell(sheet, currentRow, cell++, bill.getNetAmount());
			createCell(sheet, currentRow, cell++, bill.getVatAmount());
			cell++;
			createCell(sheet, currentRow, cell++, getLiquidationConceptBaseValue(bill, BillConcept.Stakes));
			createCell(sheet, currentRow, cell++, getLiquidationConceptBaseValue(bill, BillConcept.GGR));
			createCell(sheet, currentRow, cell++, getLiquidationConceptBaseValue(bill, BillConcept.NGR));
			createCell(sheet, currentRow, cell++, getLiquidationConceptBaseValue(bill, BillConcept.NR));
			cell++;
			createCell(sheet, currentRow, cell++, getLiquidationConceptValue(bill, BillConcept.Stakes));
			createCell(sheet, currentRow, cell++, getLiquidationConceptValue(bill, BillConcept.GGR));
			createCell(sheet, currentRow, cell++, getLiquidationConceptValue(bill, BillConcept.NGR));
			createCell(sheet, currentRow, cell++, getLiquidationConceptValue(bill, BillConcept.NR));
			currentRow++;
		}
		return currentRow;
	}

	private BigDecimal getLiquidationConceptBaseValue(Bill bill, BillConcept concept) {
		BillLiquidationDetail detail = getLiquidationConcept(bill, concept);
		return detail != null ? detail.getBaseValue() : null;
	}

	private BigDecimal getLiquidationConceptValue(Bill bill, BillConcept concept) {
		BillLiquidationDetail detail = getLiquidationConcept(bill, concept);
		return detail != null ? detail.getValue() : null;
	}

	private BillLiquidationDetail getLiquidationConcept(Bill bill, BillConcept concept) {
		for (BillLiquidationDetail i : bill.getLiquidationDetails()) {
			if (i.getConcept() == concept) {
				return i;
			}
		}
		return null;
	}

}
