package com.luckia.biller.core.reporting;

import java.io.OutputStream;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.luckia.biller.core.common.MathUtils;
import com.luckia.biller.core.common.NoAvailableDataException;
import com.luckia.biller.core.model.Bill;
import com.luckia.biller.core.model.BillLiquidationDetail;
import com.luckia.biller.core.model.CommonState;
import com.luckia.biller.core.model.Company;
import com.luckia.biller.core.model.CompanyGroup;
import com.luckia.biller.core.model.CostCenter;
import com.luckia.biller.core.model.Liquidation;
import com.luckia.biller.core.model.LiquidationDetail;
import com.luckia.biller.core.model.Store;

public class AdjustmentReportGenerator extends BaseReport {

	private static final Logger LOG = LoggerFactory.getLogger(AdjustmentReportGenerator.class);

	@Inject
	private Provider<EntityManager> entityManagerProvider;

	public void generate(Date from, Date to, Company company, CompanyGroup companyGroup, CostCenter costCenter, OutputStream out) {
		try {
			Validate.notNull(from);
			Validate.notNull(to);
			LOG.debug("Processing adjustment report between {} and {}", DateFormatUtils.ISO_DATE_FORMAT.format(from), DateFormatUtils.ISO_DATE_FORMAT.format(to));
			List<Liquidation> liquidations = loadLiquidations(from, to, company, companyGroup, costCenter);
			if (liquidations.isEmpty()) {
				throw new NoAvailableDataException();
			}
			LOG.debug("Readed {} liquidations", liquidations.size());
			HSSFWorkbook workbook = new HSSFWorkbook();
			init(workbook);
			HSSFSheet sheet = workbook.createSheet("Ajustes");
			int currentRow = 0;
			currentRow = createHeader(sheet, currentRow);
			for (int i = 0; i < liquidations.size(); i++) {
				Liquidation liquidation = liquidations.get(i);
				currentRow = createLiquidationDetails(sheet, currentRow, liquidation);
			}
			autoSizeColumns(sheet, 10);
			workbook.write(out);
			out.flush();
			out.close();
		} catch (NoAvailableDataException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new RuntimeException("Adjustment report generation error", ex);
		}
	}

	private List<Liquidation> loadLiquidations(Date from, Date to, Company company, CompanyGroup companyGroup, CostCenter costCenter) {
		StringBuffer sb = new StringBuffer();
		sb.append("select distinct(l) from Liquidation l ");
		if (costCenter != null) {
			sb.append("join Bill b on b.liquidation =  l ");
			sb.append("join Store s on b.sender = s ");
		}
		sb.append("where l.billDate >= :from and l.billDate <= :to ");
		sb.append("and l.currentState.stateDefinition.id in :states ");
		if (company != null) {
			sb.append("and l.sender = :sender ");
		}
		if (companyGroup != null) {
			sb.append("and l.sender.parent = :companyGroup ");
		}
		if (costCenter != null) {
			sb.append("and s.costCenter = :costCenter ");
		}
		sb.append("order by l.billDate, l.id");
		EntityManager entityManager = entityManagerProvider.get();
		TypedQuery<Liquidation> query = entityManager.createQuery(sb.toString(), Liquidation.class);
		query.setParameter("states", Arrays.asList(CommonState.CONFIRMED, CommonState.SENT));
		query.setParameter("from", from);
		query.setParameter("to", to);
		if (company != null) {
			query.setParameter("sender", company);
		}
		if (companyGroup != null) {
			query.setParameter("companyGroup", companyGroup);
		}
		if (costCenter != null) {
			query.setParameter("costCenter", costCenter);
		}
		return query.getResultList();
	}

	private int createHeader(HSSFSheet sheet, int currentRow) {
		int cell = 0;
		createHeaderCell(sheet, currentRow, cell++, "EMISOR");
		createHeaderCell(sheet, currentRow, cell++, "DESTINATARIO");
		createHeaderCell(sheet, currentRow, cell++, "CENTRO DE COSTE");
		createHeaderCell(sheet, currentRow, cell++, "ESTADO");
		createHeaderCell(sheet, currentRow, cell++, "FECHA");
		createHeaderCell(sheet, currentRow, cell++, "TIPO");
		createHeaderCell(sheet, currentRow, cell++, "INCLUIDO");
		createHeaderCell(sheet, currentRow, cell++, "CONCEPTO");
		createHeaderCell(sheet, currentRow, cell++, "BASE IMPONIBLE");
		createHeaderCell(sheet, currentRow, cell++, "IVA/EGIT");
		createHeaderCell(sheet, currentRow, cell++, "TOTAL");
		return ++currentRow;
	}

	private int createLiquidationDetails(HSSFSheet sheet, int currentRow, Liquidation liquidation) {
		CostCenter costCenter = null;
		if (!liquidation.getBills().isEmpty()) {
			for (Bill bill : liquidation.getBills()) {
				Store store = bill.getSender().as(Store.class);
				if (store.getCostCenter() != null) {
					costCenter = store.getCostCenter();
					break;
				}
			}
		}
		int cell = 0;
		for (LiquidationDetail i : liquidation.getDetails()) {
			cell = 0;
			createCell(sheet, currentRow, cell++, liquidation.getSender().getName());
			createCell(sheet, currentRow, cell++, liquidation.getReceiver().getName());
			createCell(sheet, currentRow, cell++, costCenter != null ? costCenter.getName() : StringUtils.EMPTY);
			createCell(sheet, currentRow, cell++, liquidation.getCurrentState().getStateDefinition().getId());
			createCell(sheet, currentRow, cell++, liquidation.getBillDate());
			createCell(sheet, currentRow, cell++, "OPERADOR");
			createCell(sheet, currentRow, cell++, i.getLiquidationIncluded() ? "SI" : "NO");
			createCell(sheet, currentRow, cell++, i.getName());
			createCell(sheet, currentRow, cell++, MathUtils.safeNull(i.getNetValue()));
			createCell(sheet, currentRow, cell++, MathUtils.safeNull(i.getVatValue()));
			createCell(sheet, currentRow, cell++, MathUtils.safeNull(i.getValue()));
			currentRow++;
		}
		for (Bill bill : liquidation.getBills()) {
			for (BillLiquidationDetail detail : bill.getLiquidationDetails()) {
				cell = 0;
				switch (detail.getConcept()) {
				case MANUAL:
					createCell(sheet, currentRow, cell++, liquidation.getSender().getName());
					createCell(sheet, currentRow, cell++, liquidation.getReceiver().getName());
					createCell(sheet, currentRow, cell++, costCenter != null ? costCenter.getName() : StringUtils.EMPTY);
					createCell(sheet, currentRow, cell++, bill.getCurrentState().getStateDefinition().getId());
					createCell(sheet, currentRow, cell++, liquidation.getBillDate());
					createCell(sheet, currentRow, cell++, "ESTABLECIMIENTO");
					createCell(sheet, currentRow, cell++, detail.getLiquidationIncluded() ? "SI" : "NO");
					createCell(sheet, currentRow, cell++, detail.getName());
					createCell(sheet, currentRow, cell++, MathUtils.safeNull(detail.getNetValue()));
					createCell(sheet, currentRow, cell++, MathUtils.safeNull(detail.getVatValue()));
					createCell(sheet, currentRow, cell++, MathUtils.safeNull(detail.getValue()));
					currentRow++;
					break;
				default:
					break;
				}
			}
		}
		return currentRow;
	}

}
