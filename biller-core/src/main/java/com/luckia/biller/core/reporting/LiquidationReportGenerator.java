package com.luckia.biller.core.reporting;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.luckia.biller.core.model.AppFile;
import com.luckia.biller.core.model.Bill;
import com.luckia.biller.core.model.BillConcept;
import com.luckia.biller.core.model.BillLiquidationDetail;
import com.luckia.biller.core.model.BillerComparator;
import com.luckia.biller.core.model.LegalEntity;
import com.luckia.biller.core.model.Liquidation;
import com.luckia.biller.core.model.Store;
import com.luckia.biller.core.model.TerminalRelation;
import com.luckia.biller.core.model.common.Message;
import com.luckia.biller.core.services.FileService;

/**
 * Componente encargado de generar los reportes de liquidaciones.
 */
public class LiquidationReportGenerator extends BaseReport {

	private static final Logger LOG = LoggerFactory.getLogger(LiquidationReportGenerator.class);

	@Inject
	private LiquidationReportDataSource dataSource;
	@Inject
	private FileService fileService;

	public Message<AppFile> generate(Date from, Date to, List<LegalEntity> entities) {
		if (from == null && to == null) {
			from = new DateTime().minusMonths(1).dayOfMonth().withMinimumValue().toDate();
			to = new DateTime().minusMonths(1).dayOfMonth().withMaximumValue().toDate();
		}
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		generate(from, to, entities, out);
		ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
		String fileName = String.format("Liquidaciones-%s-%s.xls", DateFormatUtils.ISO_DATE_FORMAT.format(from), DateFormatUtils.ISO_DATE_FORMAT.format(to));
		AppFile appFile = fileService.save(fileName, FileService.CONTENT_TYPE_EXCEL, in);
		return new Message<AppFile>(Message.CODE_SUCCESS, "Informe generado", appFile);
	}

	public Message<String> generate(Date from, Date to, List<LegalEntity> entities, OutputStream out) {
		try {
			Validate.notNull(from);
			Validate.notNull(to);
			LOG.debug("Generando informe de liquidacione entre {} y {}", DateFormatUtils.ISO_DATE_FORMAT.format(from), DateFormatUtils.ISO_DATE_FORMAT.format(to));
			for (LegalEntity i : entities) {
				LOG.debug("  Operadora: {}", i.getName());
			}
			Map<LegalEntity, List<Liquidation>> liquidationMap = dataSource.getLiquidations(from, to, entities);
			if (!liquidationMap.isEmpty()) {
				HSSFWorkbook workbook = new HSSFWorkbook();
				init(workbook);
				for (LegalEntity legalEntity : liquidationMap.keySet()) {
					List<Liquidation> liquidations = liquidationMap.get(legalEntity);
					processSheet(legalEntity, liquidations, workbook);
				}
				workbook.write(out);
				out.flush();
				out.close();
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
		LOG.debug("Procesando hoja de liquidacion del operador {}", operator.getName());
		HSSFSheet sheet = workbook.createSheet(operator.getName());
		int currentRow = 0;
		for (int i = 0; i < liquidations.size(); i++) {
			Liquidation liquidation = liquidations.get(i);
			currentRow = createHeader(sheet, currentRow++);
			currentRow = createLiquidationDetails(sheet, currentRow, liquidation);
			currentRow = createFooter(sheet, currentRow, liquidation);
			currentRow += 3;
		}
		for (int i = 0; i < 26; i++) {
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
		createHeaderCell(sheet, currentRow, cell++, "Apostado (base)");
		createHeaderCell(sheet, currentRow, cell++, "Cancelado (base)");
		createHeaderCell(sheet, currentRow, cell++, "Premios (base)");
		createHeaderCell(sheet, currentRow, cell++, "Stakes (base)");
		createHeaderCell(sheet, currentRow, cell++, "Imputable (base)");
		createHeaderCell(sheet, currentRow, cell++, "Margen (base)");
		createHeaderCell(sheet, currentRow, cell++, "GGR (base)");
		createHeaderCell(sheet, currentRow, cell++, "NGR (base)");
		createHeaderCell(sheet, currentRow, cell++, "NR (base)");
		cell++;
		createHeaderCell(sheet, currentRow, cell++, "Stakes");
		createHeaderCell(sheet, currentRow, cell++, "GGR");
		createHeaderCell(sheet, currentRow, cell++, "NGR");
		createHeaderCell(sheet, currentRow, cell++, "NR");
		cell++;
		createHeaderCell(sheet, currentRow, cell++, "Terminales");
		return currentRow + 1;
	}

	private int createFooter(HSSFSheet sheet, int currentRow, Liquidation liquidation) {
		int textCol = 3;
		int blankCol = 4;
		int valueCol = 5;
		createDisabledCell(sheet, currentRow, textCol, "Total:");
		createDisabledCell(sheet, currentRow, blankCol, StringUtils.EMPTY);
		createDisabledCell(sheet, currentRow, valueCol, liquidation.getLiquidationResults().getSenderAmount());
		currentRow++;
		createDisabledCell(sheet, currentRow, textCol, "Ajustes:");
		createDisabledCell(sheet, currentRow, blankCol, StringUtils.EMPTY);
		createDisabledCell(sheet, currentRow, valueCol, liquidation.getLiquidationResults().getAdjustmentAmount());
		currentRow++;
		createDisabledCell(sheet, currentRow, textCol, "Saldo de caja:");
		createDisabledCell(sheet, currentRow, blankCol, StringUtils.EMPTY);
		createDisabledCell(sheet, currentRow, valueCol, liquidation.getLiquidationResults().getCashStoreAmount());
		currentRow++;
		createDisabledCell(sheet, currentRow, textCol, "Resultado:");
		createDisabledCell(sheet, currentRow, blankCol, StringUtils.EMPTY);
		createDisabledCell(sheet, currentRow, valueCol, liquidation.getLiquidationResults().getReceiverAmount());
		return currentRow + 1;

	}

	private int createLiquidationDetails(HSSFSheet sheet, int currentRow, Liquidation liquidation) {
		Collections.sort(liquidation.getBills(), new BillerComparator());
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
			createCell(sheet, currentRow, cell++, getLiquidationConceptBaseValue(bill, BillConcept.TotalBetAmount));
			createCell(sheet, currentRow, cell++, getLiquidationConceptBaseValue(bill, BillConcept.Cance1lled));
			createCell(sheet, currentRow, cell++, getLiquidationConceptBaseValue(bill, BillConcept.TotalWinAmount));
			createCell(sheet, currentRow, cell++, getLiquidationConceptBaseValue(bill, BillConcept.Stakes));
			createCell(sheet, currentRow, cell++, getLiquidationConceptBaseValue(bill, BillConcept.TotalAttributable));
			createCell(sheet, currentRow, cell++, getLiquidationConceptBaseValue(bill, BillConcept.Margin));
			createCell(sheet, currentRow, cell++, getLiquidationConceptBaseValue(bill, BillConcept.GGR));
			createCell(sheet, currentRow, cell++, getLiquidationConceptBaseValue(bill, BillConcept.NGR));
			createCell(sheet, currentRow, cell++, getLiquidationConceptBaseValue(bill, BillConcept.NR));
			cell++;
			createCell(sheet, currentRow, cell++, getLiquidationConceptValue(bill, BillConcept.Stakes));
			createCell(sheet, currentRow, cell++, getLiquidationConceptValue(bill, BillConcept.GGR));
			createCell(sheet, currentRow, cell++, getLiquidationConceptValue(bill, BillConcept.NGR));
			createCell(sheet, currentRow, cell++, getLiquidationConceptValue(bill, BillConcept.NR));
			cell++;
			StringBuffer sb = new StringBuffer();
			if (Store.class.isAssignableFrom(bill.getSender().getClass())) {
				Store store = bill.getSender().as(Store.class);
				for (Iterator<TerminalRelation> iterator = store.getTerminalRelations().iterator(); iterator.hasNext();) {
					TerminalRelation relation = iterator.next();
					sb.append(relation.getCode());
					if (iterator.hasNext()) {
						sb.append(", ");
					}

				}
			}
			createCell(sheet, currentRow, cell++, sb.toString());
			currentRow++;
		}
		return currentRow;
	}

	private BigDecimal getLiquidationConceptBaseValue(Bill bill, BillConcept concept) {
		BigDecimal result = BigDecimal.ZERO;
		if (bill.getBillingRawData() != null && bill.getBillingRawData().containsKey(concept)) {
			result = bill.getBillingRawData().get(concept);
		}
		return result;
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
