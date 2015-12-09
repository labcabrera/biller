package com.luckia.biller.core.reporting;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.luckia.biller.core.common.MathUtils;
import com.luckia.biller.core.model.AppFile;
import com.luckia.biller.core.model.CompanyGroup;
import com.luckia.biller.core.model.CostCenter;
import com.luckia.biller.core.model.LegalEntity;
import com.luckia.biller.core.model.Liquidation;
import com.luckia.biller.core.model.LiquidationDetail;
import com.luckia.biller.core.model.common.Message;
import com.luckia.biller.core.services.FileService;

public class LiquidationSummaryReportGenerator extends BaseReport {

	private static final Logger LOG = LoggerFactory.getLogger(LiquidationSummaryReportGenerator.class);
	private static final String DATE_FORMAT = "dd-MM-yyyy";

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
			createReportData(sheet, from, to, company, costCenter);
			for (int i = 0; i < 30; i++) {
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
			int col = 0;
			BigDecimal amount = MathUtils.safeNull(liquidation.getLiquidationResults().getTotalAmount());
			BigDecimal cashStore = MathUtils.safeNull(liquidation.getLiquidationResults().getCashStoreAmount());
			BigDecimal adjustements = MathUtils.safeNull(getAdjustementAmount(liquidation));
			BigDecimal result = MathUtils.safeNull(liquidation.getLiquidationResults().getReceiverAmount());
			totalAmount = totalAmount.add(amount);
			totalCashStore = totalCashStore.add(cashStore);
			totalAdjustements = totalAdjustements.add(adjustements);
			totalResults = totalResults.add(result);
			createCell(sheet, rowIndex, col++, ISODateTimeFormat.date().print(new DateTime(liquidation.getBillDate())));
			createCell(sheet, rowIndex, col++, liquidation.getReceiver().getName());
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
		CriteriaBuilder builder = entityManager.getCriteriaBuilder();
		CriteriaQuery<Liquidation> criteria = builder.createQuery(Liquidation.class);
		Root<Liquidation> root = criteria.from(Liquidation.class);
		List<Predicate> predicates = new ArrayList<>();
		if (from != null) {
			predicates.add(builder.greaterThanOrEqualTo(root.<Date> get("billDate"), from));
		}
		if (to != null) {
			predicates.add(builder.lessThanOrEqualTo(root.<Date> get("billDate"), to));
		}
		if (company != null) {
			predicates.add(builder.equal(root.get("sender"), company));
		}
		criteria.where(predicates.toArray(new Predicate[predicates.size()]));
		criteria.orderBy(builder.asc(root.<Date> get("billDate")), builder.asc(root.<String> get("sender").get("name")), builder.desc(root.<String> get("code")));
		TypedQuery<Liquidation> query = entityManager.createQuery(criteria);
		return query.getResultList();
	}

	private void configureHeaders(HSSFSheet sheet) {
		int index = 0;
		int col = 0;
		createHeaderCell(sheet, col, index++, "FECHA DE LIQUIDACION");
		createHeaderCell(sheet, col, index++, "GRUPO");
		createHeaderCell(sheet, col, index++, "OPERADORA");
		createHeaderCell(sheet, col, index++, "IMPORTE DE LIQUIDACION");
		createHeaderCell(sheet, col, index++, "SALDO DE CAJA");
		createHeaderCell(sheet, col, index++, "AJUSTES MANUALES");
		createHeaderCell(sheet, col, index++, "RESULTADO");
		createHeaderCell(sheet, col, index++, "APOSTADO");
		createHeaderCell(sheet, col, index++, "PAGADO");
		createHeaderCell(sheet, col, index++, "CREDITO");
		createHeaderCell(sheet, col, index++, "AJUSTES MANUALES");
		createHeaderCell(sheet, col, index++, "RESULTADO A PERCIBIR");
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
