package com.luckia.biller.core.reporting;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.luckia.biller.core.common.MathUtils;
import com.luckia.biller.core.common.NoAvailableDataException;
import com.luckia.biller.core.model.AppFile;
import com.luckia.biller.core.model.Bill;
import com.luckia.biller.core.model.BillConcept;
import com.luckia.biller.core.model.BillLiquidationDetail;
import com.luckia.biller.core.model.BillRawData;
import com.luckia.biller.core.model.CompanyGroup;
import com.luckia.biller.core.model.CostCenter;
import com.luckia.biller.core.model.LegalEntity;
import com.luckia.biller.core.model.Liquidation;
import com.luckia.biller.core.model.LiquidationDetail;
import com.luckia.biller.core.model.common.Message;
import com.luckia.biller.core.services.FileService;

/**
 * Componente encargado de generar el report de resumen de liquidaciones.
 */
public class LiquidationSummaryReportGenerator extends BaseReport {

	private static final Logger LOG = LoggerFactory.getLogger(LiquidationSummaryReportGenerator.class);
	private static final String DATE_FORMAT = "dd-MM-yyyy";

	@Inject
	private FileService fileService;
	@Inject
	private LiquidationReportDataSource dataSource;

	/**
	 * 
	 * @param from
	 *            fecha desde (obligatorio)
	 * @param to
	 *            fecha hasta (obligatorio)
	 * @param company
	 *            operadora (opcional)
	 * @param costCenter
	 *            centro de coste (opcional)
	 * @param companyGroup
	 *            grupo de empresas (opcional)
	 * @return
	 * @throws IOException
	 */
	public Message<AppFile> generate(Date from, Date to, LegalEntity company, CostCenter costCenter, CompanyGroup companyGroup) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		generate(from, to, company, costCenter, companyGroup, out);
		ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
		String fileName = String.format("Resumen-liquidaciones.xls");
		AppFile appFile = fileService.save(fileName, FileService.CONTENT_TYPE_EXCEL, in);
		return new Message<AppFile>(Message.CODE_SUCCESS, "Report file created", appFile);
	}

	public Message<AppFile> generate(Date from, Date to, LegalEntity company, CostCenter costCenter, CompanyGroup companyGroup, OutputStream out) {
		LOG.debug("Generating liquidation summary report");
		try {
			HSSFWorkbook workbook = new HSSFWorkbook();
			init(workbook);
			StringBuilder sheetName = new StringBuilder("Resumen liquidaciones");
			if (from != null) {
				sheetName.append(" ");
				sheetName.append(new SimpleDateFormat(DATE_FORMAT).format(from));
			}
			if (to != null) {
				sheetName.append(" ");
				sheetName.append(new SimpleDateFormat(DATE_FORMAT).format(to));
			}
			HSSFSheet sheet = workbook.createSheet(sheetName.toString());
			configureHeaders(sheet);
			createReportData(sheet, from, to, company, costCenter, companyGroup);
			autoSizeColumns(sheet, 40);
			workbook.write(out);
			out.flush();
			out.close();
			return new Message<>(Message.CODE_SUCCESS, "Report generated");
		} catch (NoAvailableDataException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new RuntimeException("Error generating summary liquidation report", ex);
		}
	}

	private void createReportData(HSSFSheet sheet, Date from, Date to, LegalEntity company, CostCenter costCenter, CompanyGroup companyGroup) {
		List<Liquidation> liquidations = dataSource.findLiquidations(from, to, company, costCenter, companyGroup);
		LOG.debug("Readed {} liquidations", liquidations.size());
		if (liquidations.isEmpty()) {
			throw new NoAvailableDataException();
		}
		BigDecimal totalAmount = BigDecimal.ZERO;
		BigDecimal totalCashStore = BigDecimal.ZERO;
		BigDecimal totalAdjustements = BigDecimal.ZERO;
		BigDecimal totalResults = BigDecimal.ZERO;
		int rowIndex = 1;
		for (Liquidation liquidation : liquidations) {
			int col = 0;
			BigDecimal amount = MathUtils.safeNull(liquidation.getLiquidationResults().getTotalAmount());
			BigDecimal cashStore = MathUtils.safeNull(liquidation.getLiquidationResults().getCashStoreAmount());
			BigDecimal outerAdjustements = MathUtils.safeNull(getAdjustementOuterAmount(liquidation));
			BigDecimal result = MathUtils.safeNull(liquidation.getLiquidationResults().getReceiverAmount());
			BigDecimal betAmount = getAmountByConcept(liquidation, BillConcept.TOTAL_BET_AMOUNT);
			BigDecimal winAmount = getAmountByConcept(liquidation, BillConcept.TOTAL_WIN_AMOUNT);
			BigDecimal credit = getAmountByConcept(liquidation, BillConcept.CREDIT);
			BigDecimal innerAdjustements = MathUtils.safeNull(getAdjustementInnerAmount(liquidation));
			totalAmount = totalAmount.add(amount);
			totalCashStore = totalCashStore.add(cashStore);
			totalAdjustements = totalAdjustements.add(outerAdjustements);
			totalResults = totalResults.add(result);
			createCell(sheet, rowIndex, col++, ISODateTimeFormat.date().print(new DateTime(liquidation.getBillDate())));
			createCell(sheet, rowIndex, col++, liquidation.getReceiver().getName());
			createCell(sheet, rowIndex, col++, liquidation.getSender().getName());
			createCell(sheet, rowIndex, col++, amount);
			createCell(sheet, rowIndex, col++, cashStore);
			createCell(sheet, rowIndex, col++, outerAdjustements);
			createCell(sheet, rowIndex, col++, result);
			createCell(sheet, rowIndex, col++, betAmount);
			createCell(sheet, rowIndex, col++, winAmount);
			createCell(sheet, rowIndex, col++, credit);
			createCell(sheet, rowIndex, col++, innerAdjustements);
			rowIndex++;
		}
		rowIndex++;
		int col = 2;
		createHeaderCell(sheet, rowIndex, col++, "TOTAL");
		createHeaderCell(sheet, rowIndex, col++, totalAmount);
		createHeaderCell(sheet, rowIndex, col++, totalCashStore);
		createHeaderCell(sheet, rowIndex, col++, totalAmount);
		createHeaderCell(sheet, rowIndex, col++, totalResults);
	}

	private void configureHeaders(HSSFSheet sheet) {
		int index = 0;
		int col = 0;
		createHeaderCell(sheet, col, index++, "FECHA DE LIQUIDACION");
		createHeaderCell(sheet, col, index++, "GRUPO");
		createHeaderCell(sheet, col, index++, "OPERADORA");
		createHeaderCell(sheet, col, index++, "IMPORTE DE LIQUIDACION");
		createHeaderCell(sheet, col, index++, "SALDO DE CAJA");
		createHeaderCell(sheet, col, index++, "AJUSTES EXTERNOS");
		createHeaderCell(sheet, col, index++, "RESULTADO");
		createHeaderCell(sheet, col, index++, "APOSTADO");
		createHeaderCell(sheet, col, index++, "PAGADO");
		createHeaderCell(sheet, col, index++, "CREDITO");
		createHeaderCell(sheet, col, index++, "AJUSTES INTERNOS");
	}

	private BigDecimal getAdjustementOuterAmount(Liquidation liquidation) {
		BigDecimal result = BigDecimal.ZERO;
		for (Bill bill : liquidation.getBills()) {
			for (BillLiquidationDetail i : bill.getLiquidationDetails()) {
				if (!i.getLiquidationIncluded()) {
					result = result.add(MathUtils.safeNull(i.getValue()));
				}
			}
		}
		if (liquidation.getDetails() != null) {
			for (LiquidationDetail detail : liquidation.getDetails()) {
				if (!detail.getLiquidationIncluded()) {
					result = result.add(MathUtils.safeNull(detail.getValue()));
				}
			}
		}
		return result;
	}

	private BigDecimal getAdjustementInnerAmount(Liquidation liquidation) {
		BigDecimal result = BigDecimal.ZERO;
		for (Bill bill : liquidation.getBills()) {
			for (BillLiquidationDetail i : bill.getLiquidationDetails()) {
				if (i.getLiquidationIncluded()) {
					result = result.add(MathUtils.safeNull(i.getValue()));
				}
			}
		}
		if (liquidation.getDetails() != null) {
			for (LiquidationDetail detail : liquidation.getDetails()) {
				if (detail.getLiquidationIncluded()) {
					result = result.add(MathUtils.safeNull(detail.getValue()));
				}
			}
		}
		return result;
	}

	private BigDecimal getAmountByConcept(Liquidation liquidation, BillConcept concept) {
		BigDecimal result = BigDecimal.ZERO;
		for (Bill bill : liquidation.getBills()) {
			for (BillRawData i : bill.getBillRawData()) {
				if (i.getConcept() == concept) {
					result = result.add(MathUtils.safeNull(i.getAmount()));
				}
			}
		}
		return result;
	}

}
