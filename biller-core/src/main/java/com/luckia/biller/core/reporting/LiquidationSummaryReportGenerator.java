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
import javax.inject.Provider;
import javax.persistence.EntityManager;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.luckia.biller.core.model.AppFile;
import com.luckia.biller.core.model.CostCenter;
import com.luckia.biller.core.model.LegalEntity;
import com.luckia.biller.core.model.Liquidation;
import com.luckia.biller.core.model.LiquidationDetail;
import com.luckia.biller.core.model.common.Message;
import com.luckia.biller.core.services.FileService;

public class LiquidationSummaryReportGenerator extends BaseReport {

	private static final Logger LOG = LoggerFactory.getLogger(LiquidationSummaryReportGenerator.class);

	@Inject
	private FileService fileService;
	@Inject
	private Provider<EntityManager> entityManagerProvider;

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
	 * @return
	 * @throws IOException
	 */
	public Message<AppFile> generate(Date from, Date to, LegalEntity company, CostCenter costCenter) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		generate(from, to, company, costCenter, out);
		ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
		String fileName = String.format("Resumen-liquidaciones-%s_%s.xls", DateFormatUtils.ISO_DATE_FORMAT.format(from), DateFormatUtils.ISO_DATE_FORMAT.format(to));
		AppFile appFile = fileService.save(fileName, FileService.CONTENT_TYPE_EXCEL, in);
		return new Message<AppFile>(Message.CODE_SUCCESS, "Report file created", appFile);
	}

	public Message<AppFile> generate(Date from, Date to, LegalEntity company, CostCenter costCenter, OutputStream out) {
		LOG.debug("Generating liquidation summary report");
		try {
			HSSFWorkbook workbook = new HSSFWorkbook();
			init(workbook);
			String sheetName = String.format("Resumen liquidaciones %s_%s", new SimpleDateFormat("dd-MM-yyyy").format(from), new SimpleDateFormat("dd-MM-yyyy").format(to));
			HSSFSheet sheet = workbook.createSheet(sheetName);
			configureHeaders(sheet);
			createReportData(sheet, from, to, company, costCenter);
			for (int i = 0; i < 7; i++) {
				sheet.autoSizeColumn(i);
			}
			workbook.write(out);
			out.flush();
			out.close();
			return new Message<>(Message.CODE_SUCCESS, "Report generated");
		} catch (Exception ex) {
			throw new RuntimeException("Error generating summary liquidation report", ex);
		}
	}

	private void createReportData(HSSFSheet sheet, Date from, Date to, LegalEntity company, CostCenter costCenter) {
		List<Liquidation> liquidations = findLiquidations(from, to, company, costCenter);
		BigDecimal totalAmount = BigDecimal.ZERO;
		BigDecimal totalCashStore = BigDecimal.ZERO;
		BigDecimal totalAdjustements = BigDecimal.ZERO;
		BigDecimal totalResults = BigDecimal.ZERO;
		int rowIndex = 1;
		for (Liquidation liquidation : liquidations) {
			int col = 1;
			BigDecimal amount = liquidation.getAmount() != null ? liquidation.getAmount() : BigDecimal.ZERO;
			BigDecimal cashStore = liquidation.getLiquidationResults().getCashStoreAmount();
			BigDecimal adjustements = getAdjustementAmount(liquidation);
			BigDecimal result = liquidation.getLiquidationResults().getReceiverAmount();
			totalAmount = totalAmount.add(amount);
			totalCashStore = totalCashStore.add(cashStore);
			totalAdjustements = totalAdjustements.add(adjustements);
			totalResults = totalResults.add(result);
			createCell(sheet, rowIndex, col++, ISODateTimeFormat.date().print(new DateTime(liquidation.getBillDate())));
			createCell(sheet, rowIndex, col++, liquidation.getSender().getName());
			createCell(sheet, rowIndex, col++, amount);
			createCell(sheet, rowIndex, col++, cashStore);
			createCell(sheet, rowIndex, col++, adjustements);
			createCell(sheet, rowIndex, col++, result);
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

	private List<Liquidation> findLiquidations(Date from, Date to, LegalEntity company, CostCenter costCenter) {
		EntityManager entityManager = entityManagerProvider.get();
		return entityManager.createQuery("select e from Liquidation e", Liquidation.class).setMaxResults(10).getResultList();
	}

	private void configureHeaders(HSSFSheet sheet) {
		int index = 1;
		int col = 0;
		createHeaderCell(sheet, col, index++, "Fecha de liquidaciñón");
		createHeaderCell(sheet, col, index++, "Operadora");
		createHeaderCell(sheet, col, index++, "Importe");
		createHeaderCell(sheet, col, index++, "Saldo de caja");
		createHeaderCell(sheet, col, index++, "Ajustes manuales");
		createHeaderCell(sheet, col, index++, "Resultado a percibir por EH");
	}

	private BigDecimal getAdjustementAmount(Liquidation liquidation) {
		BigDecimal result = BigDecimal.ZERO;
		if (liquidation.getDetails() != null) {
			for (LiquidationDetail detail : liquidation.getDetails()) {
				result = result.add(detail.getValue());
			}
		}
		return result;
	}
}