package com.luckia.biller.core.reporting;

import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.util.CellRangeAddress;

import com.luckia.biller.core.common.MathUtils;
import com.luckia.biller.core.model.Bill;
import com.luckia.biller.core.model.BillConcept;
import com.luckia.biller.core.model.BillLiquidationDetail;
import com.luckia.biller.core.model.BillRawData;
import com.luckia.biller.core.model.BillerComparator;
import com.luckia.biller.core.model.Company;
import com.luckia.biller.core.model.CompanyGroup;
import com.luckia.biller.core.model.CostCenter;
import com.luckia.biller.core.model.LegalEntity;
import com.luckia.biller.core.model.Liquidation;
import com.luckia.biller.core.model.Store;
import com.luckia.biller.core.model.TerminalRelation;
import com.luckia.biller.core.model.common.Message;

import lombok.extern.slf4j.Slf4j;

/**
 * Componente encargado de generar los reportes de liquidaciones.
 */
@Slf4j
public class LiquidationReportGenerator extends BaseReport {

	@Inject
	private LiquidationReportDataSource dataSource;

	public Message<String> generate(Date from, Date to, Company company,
			CompanyGroup companyGroup, CostCenter costCenter, OutputStream out) {
		try {
			Validate.notNull(from, "Missing date from");
			Validate.notNull(to, "Missing date to");
			log.debug("Generando informe de liquidacione entre {} y {}",
					DateFormatUtils.ISO_DATE_FORMAT.format(from),
					DateFormatUtils.ISO_DATE_FORMAT.format(to));
			List<Liquidation> allLiquidations = dataSource.findLiquidations(from, to,
					company, costCenter, companyGroup);
			Map<LegalEntity, List<Liquidation>> liquidationMap = dataSource
					.groupByCompany(allLiquidations);
			if (!liquidationMap.isEmpty()) {
				HSSFWorkbook workbook = new HSSFWorkbook();
				init(workbook);
				for (Entry<LegalEntity, List<Liquidation>> i : liquidationMap
						.entrySet()) {
					LegalEntity legalEntity = i.getKey();
					List<Liquidation> liquidations = liquidationMap.get(legalEntity);
					processSheet(legalEntity, liquidations, workbook);
				}
				workbook.write(out);
				out.flush();
				out.close();
				return new Message<>(Message.CODE_SUCCESS, String.format(
						"Generado report de %s liquidaciones", liquidationMap.size()));
			}
			else {
				return new Message<>(Message.CODE_SUCCESS,
						"No se han encontrado liquidaciones");
			}
		}
		catch (Exception ex) {
			log.error("Error al generar el report de liquidaciones", ex);
			return new Message<>(Message.CODE_GENERIC_ERROR,
					"Error al generar el report de liquidaciones");
		}
	}

	private void processSheet(LegalEntity operator, List<Liquidation> liquidations,
			HSSFWorkbook workbook) {
		log.debug("Procesando hoja de liquidacion del operador {}", operator.getName());
		HSSFSheet sheet = workbook.createSheet(operator.getName());
		int currentRow = 0;
		for (int i = 0; i < liquidations.size(); i++) {
			Liquidation liquidation = liquidations.get(i);
			currentRow = createHeader(sheet, currentRow++);
			currentRow = createLiquidationDetails(sheet, currentRow, liquidation);
			currentRow = createFooter(sheet, currentRow, liquidation);
			currentRow += 3;
		}
		autoSizeColumns(sheet, 40);
	}

	private int createHeader(HSSFSheet sheet, int currentRow) {
		createHeaderCell(sheet, currentRow, 2, "RESUMEN");
		createHeaderCell(sheet, currentRow, 11, "FACTURA");
		createHeaderCell(sheet, currentRow, 14, "DETALLE LIQUIDACION");
		createHeaderCell(sheet, currentRow, 25, "MODELO APLICADO");
		createHeaderCell(sheet, currentRow, 30, "GASTOS DIRECTOS");
		sheet.addMergedRegion(new CellRangeAddress(currentRow, currentRow, 2, 9));
		sheet.addMergedRegion(new CellRangeAddress(currentRow, currentRow, 11, 12));
		sheet.addMergedRegion(new CellRangeAddress(currentRow, currentRow, 14, 23));
		sheet.addMergedRegion(new CellRangeAddress(currentRow, currentRow, 24, 28));
		sheet.addMergedRegion(new CellRangeAddress(currentRow, currentRow, 28, 32));
		currentRow++;
		int cell = 0;
		createHeaderCell(sheet, currentRow, cell++, "OPERADORA");
		createHeaderCell(sheet, currentRow, cell++, "LOCAL");
		createHeaderCell(sheet, currentRow, cell++, "FECHA");
		createHeaderCell(sheet, currentRow, cell++, "ESTADO");
		createHeaderCell(sheet, currentRow, cell++, "FACTURA");
		createHeaderCell(sheet, currentRow, cell++, "LIQUIDACIÓN");
		createHeaderCell(sheet, currentRow, cell++, "DESDE");
		createHeaderCell(sheet, currentRow, cell++, "HASTA");
		createHeaderCell(sheet, currentRow, cell++, "SALDO DE CAJA");
		createHeaderCell(sheet, currentRow, cell++, "MODELO");
		cell++;
		createHeaderCell(sheet, currentRow, cell++, "IMPORTE NETO");
		createHeaderCell(sheet, currentRow, cell++, "IVA / IGIT");
		cell++;
		createHeaderCell(sheet, currentRow, cell++, "APOSTADO (BASE)");
		createHeaderCell(sheet, currentRow, cell++, "CANCELADO (BASE)");
		createHeaderCell(sheet, currentRow, cell++, "APOSTADO EFECTIVO (BASE)");
		createHeaderCell(sheet, currentRow, cell++, "PAGADO (BASE)");
		createHeaderCell(sheet, currentRow, cell++, "IMPUTABLE (BASE)");
		createHeaderCell(sheet, currentRow, cell++, "CREDITO");
		createHeaderCell(sheet, currentRow, cell++, "SALDO DE CAJA (BASE)");
		createHeaderCell(sheet, currentRow, cell++, "GGR (BASE)");
		createHeaderCell(sheet, currentRow, cell++, "NGR (BASE)");
		createHeaderCell(sheet, currentRow, cell++, "NR (BASE)");
		cell++;
		createHeaderCell(sheet, currentRow, cell++, "GGR");
		createHeaderCell(sheet, currentRow, cell++, "NGR");
		createHeaderCell(sheet, currentRow, cell++, "NR");
		cell++;
		createHeaderCell(sheet, currentRow, cell++, "APUESTAS");
		createHeaderCell(sheet, currentRow, cell++, "SAT");
		createHeaderCell(sheet, currentRow, cell++, "ATC");
		createHeaderCell(sheet, currentRow, cell++, "CO-EXPLOTACIÓN");
		createHeaderCell(sheet, currentRow, cell++, "COSTE UBICACION");
		createHeaderCell(sheet, currentRow, cell++, "OTROS");
		cell++;
		createHeaderCell(sheet, currentRow, cell++, "TERMINALES");
		return currentRow + 1;
	}

	private int createFooter(HSSFSheet sheet, int currentRow, Liquidation liquidation) {
		int textCol = 3;
		int blankCol = 4;
		int valueCol = 5;
		createDisabledCell(sheet, currentRow, textCol, "AJUSTES");
		createDisabledCell(sheet, currentRow, blankCol, StringUtils.EMPTY);
		createDisabledCell(sheet, currentRow, valueCol, MathUtils.safeNull(
				liquidation.getLiquidationResults().getLiquidationManualInnerAmount()));
		currentRow++;
		createDisabledCell(sheet, currentRow, textCol, "TOTAL");
		createDisabledCell(sheet, currentRow, blankCol, StringUtils.EMPTY);
		createDisabledCell(sheet, currentRow, valueCol,
				MathUtils.safeNull(liquidation.getLiquidationResults().getTotalAmount()));
		currentRow++;
		createDisabledCell(sheet, currentRow, textCol, "SALDO DE CAJA");
		createDisabledCell(sheet, currentRow, blankCol, StringUtils.EMPTY);
		createDisabledCell(sheet, currentRow, valueCol, MathUtils
				.safeNull(liquidation.getLiquidationResults().getCashStoreAmount()));
		currentRow++;
		createDisabledCell(sheet, currentRow, textCol, "AJUSTES DE CAJA");
		createDisabledCell(sheet, currentRow, blankCol, StringUtils.EMPTY);
		createDisabledCell(sheet, currentRow, valueCol, MathUtils.safeNull(
				liquidation.getLiquidationResults().getLiquidationManualOuterAmount()));
		currentRow++;
		createDisabledCell(sheet, currentRow, textCol, "RESULTADO");
		createDisabledCell(sheet, currentRow, blankCol, StringUtils.EMPTY);
		createDisabledCell(sheet, currentRow, valueCol, MathUtils.safeNull(
				liquidation.getLiquidationResults().getEffectiveLiquidationAmount()));
		return currentRow + 1;

	}

	private int createLiquidationDetails(HSSFSheet sheet, int currentRow,
			Liquidation liquidation) {
		Collections.sort(liquidation.getBills(), new BillerComparator());
		for (int i = 0; i < liquidation.getBills().size(); i++) {
			Bill bill = liquidation.getBills().get(i);
			BigDecimal credit = MathUtils.safeNull(getCredit(bill));
			BigDecimal saldoCaja = BigDecimal.ZERO;
			saldoCaja = saldoCaja.add(MathUtils.safeNull(
					getLiquidationConceptBaseValue(bill, BillConcept.TOTAL_BET_AMOUNT)));
			saldoCaja = saldoCaja.subtract(MathUtils.safeNull(
					getLiquidationConceptBaseValue(bill, BillConcept.CANCELLED)));
			saldoCaja = saldoCaja.subtract(MathUtils.safeNull(
					getLiquidationConceptBaseValue(bill, BillConcept.TOTAL_WIN_AMOUNT)));
			saldoCaja = saldoCaja.add(credit);

			int cell = 0;
			createCell(sheet, currentRow, cell++, liquidation.getSender().getName());
			createCell(sheet, currentRow, cell++, bill.getSender().getName());
			createCell(sheet, currentRow, cell++, bill.getBillDate());
			// TODO i18n state definition
			createCell(sheet, currentRow, cell++,
					bill.getCurrentState().getStateDefinition().getId());
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
			createCell(sheet, currentRow, cell++,
					getLiquidationConceptBaseValue(bill, BillConcept.TOTAL_BET_AMOUNT));
			createCell(sheet, currentRow, cell++,
					getLiquidationConceptBaseValue(bill, BillConcept.CANCELLED));
			createCell(sheet, currentRow, cell++,
					getLiquidationConceptBaseValue(bill, BillConcept.STAKES));
			createCell(sheet, currentRow, cell++,
					getLiquidationConceptBaseValue(bill, BillConcept.TOTAL_WIN_AMOUNT));
			createCell(sheet, currentRow, cell++,
					getLiquidationConceptBaseValue(bill, BillConcept.TOTAL_ATTRIBUTABLE));
			createCell(sheet, currentRow, cell++, credit);
			createCell(sheet, currentRow, cell++, saldoCaja);
			createCell(sheet, currentRow, cell++,
					getLiquidationConceptBaseValue(bill, BillConcept.GGR));
			createCell(sheet, currentRow, cell++,
					getLiquidationConceptBaseValue(bill, BillConcept.NGR));
			createCell(sheet, currentRow, cell++,
					getLiquidationConceptBaseValue(bill, BillConcept.NR));
			cell++;
			createCell(sheet, currentRow, cell++,
					getLiquidationConceptValue(bill, BillConcept.GGR));
			createCell(sheet, currentRow, cell++,
					getLiquidationConceptValue(bill, BillConcept.NGR));
			createCell(sheet, currentRow, cell++,
					getLiquidationConceptValue(bill, BillConcept.NR));
			cell++;
			createCell(sheet, currentRow, cell++,
					getLiquidationConceptValue(bill, BillConcept.STAKES));
			createCell(sheet, currentRow, cell++,
					getLiquidationConceptValue(bill, BillConcept.SAT_MONTHLY_FEES));
			createCell(sheet, currentRow, cell++, getLiquidationConceptValue(bill,
					BillConcept.COMMERCIAL_MONTHLY_FEES));
			createCell(sheet, currentRow, cell++,
					bill.getModel().getCompanyModel().getCoOperatingMonthlyFees());
			createCell(sheet, currentRow, cell++,
					getLiquidationConceptValue(bill, BillConcept.PRICE_PER_LOCATION));
			// los ajustes ahora no se reflejan aqui
			createCell(sheet, currentRow, cell++, BigDecimal.ZERO);
			cell++;
			StringBuilder sb = new StringBuilder();
			if (Store.class.isAssignableFrom(bill.getSender().getClass())) {
				Store store = bill.getSender().as(Store.class);
				for (Iterator<TerminalRelation> iterator = store.getTerminalRelations()
						.iterator(); iterator.hasNext();) {
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
		if (bill.getBillRawData() != null) {
			for (BillRawData i : bill.getBillRawData()) {
				if (i.getConcept() == concept) {
					return i.getAmount();
				}
			}
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

	private BigDecimal getCredit(Bill bill) {
		BigDecimal result = BigDecimal.ZERO;
		if (bill.getBillRawData() != null) {
			for (BillRawData i : bill.getBillRawData()) {
				if (i.getConcept() == BillConcept.CREDIT) {
					result = i.getAmount();
					break;
				}
			}
		}
		return result;
	}
}
