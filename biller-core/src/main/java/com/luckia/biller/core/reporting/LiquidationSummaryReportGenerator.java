package com.luckia.biller.core.reporting;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.luckia.biller.core.model.AppFile;
import com.luckia.biller.core.model.CostCenter;
import com.luckia.biller.core.model.LegalEntity;
import com.luckia.biller.core.model.common.Message;

public class LiquidationSummaryReportGenerator extends BaseReport {

	private static final Logger LOG = LoggerFactory.getLogger(LiquidationSummaryReportGenerator.class);

	/**
	 * 
	 * @param from
	 *            obligatorio
	 * @param to
	 *            obligatorio
	 * @param operadora
	 *            opcional
	 * @param costCenter
	 *            opcional
	 * @return
	 */
	public Message<AppFile> generate(Date from, Date to, LegalEntity operadora, CostCenter costCenter) {
		LOG.debug("Generating liquidation summary report");
		HSSFWorkbook workbook = new HSSFWorkbook();
		init(workbook);
		String sheetName = String.format("Liquidaciones %s %s", new SimpleDateFormat("dd-MM-yyyy").format(from), new SimpleDateFormat("dd-MM-yyyy").format(to));
		HSSFSheet sheet = workbook.createSheet(sheetName);
		configureHeaders(sheet);
		return null;
	}

	private void configureHeaders(HSSFSheet sheet) {
		int index = 0;
		createHeaderCell(sheet, 0, index++, "Fecha de liquidaciñón");
		createHeaderCell(sheet, 0, index++, "Operadora");
		createHeaderCell(sheet, 0, index++, "Importe");
		createHeaderCell(sheet, 0, index++, "Saldo de caja");
		createHeaderCell(sheet, 0, index++, "Ajustes manuales");
		createHeaderCell(sheet, 0, index++, "Resultado a percibir por EH");
		createHeaderCell(sheet, 0, index++, "Total");
	}

}
