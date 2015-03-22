package com.luckia.biller.core.reporting;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.joda.time.DateTime;

import com.luckia.biller.core.model.Bill;
import com.luckia.biller.core.model.LegalEntity;
import com.luckia.biller.core.model.Liquidation;

/**
 * Componente encargado de generar los reportes de liquidaciones.
 */
public class LiquidationReportGenerator extends BaseReport {

	private final LiquidationReportDataSource dataSource;

	public static void main(String[] args) {
		LiquidationReportDataSource dataSource = new LiquidationReportDataSource();
		LiquidationReportGenerator generator = new LiquidationReportGenerator(dataSource);
		Date from = new DateTime(2015, 1, 1, 0, 0, 0, 0).toDate();
		Date to = new DateTime(2015, 1, 1, 0, 0, 0, 0).toDate();
		List<LegalEntity> legalEntities = Arrays.asList(dataSource.createLegalEntity("Test"));
		generator.generate(from, to, legalEntities);
		System.out.println("OK");
	}

	@Inject
	public LiquidationReportGenerator(LiquidationReportDataSource dataSource) {
		this.dataSource = dataSource;
	}

	public void generate(Date from, Date to, List<LegalEntity> entities) {
		try {
			FileOutputStream fileOut = new FileOutputStream("./target/liquidations.xls");
			HSSFWorkbook workbook = new HSSFWorkbook();

			Map<LegalEntity, List<Liquidation>> liquidationMap = dataSource.getLiquidations(from, to, entities);
			for (LegalEntity legalEntity : liquidationMap.keySet()) {
				List<Liquidation> liquidations = liquidationMap.get(legalEntity);
				processSheet(legalEntity, liquidations, workbook);
			}
			workbook.write(fileOut);
			fileOut.flush();
			fileOut.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void processSheet(LegalEntity operator, List<Liquidation> liquidations, HSSFWorkbook workbook) {
		HSSFSheet sheet = workbook.createSheet(operator.getName());
		int currentRow = 0;
		for (int i = 0; i < liquidations.size(); i++) {
			Liquidation liquidation = liquidations.get(i);
			currentRow = createHeader(sheet, currentRow++);
			currentRow = createLiquidationDetails(sheet, currentRow, liquidation);
			currentRow += 3;
		}
	}

	private int createHeader(HSSFSheet sheet, int currentRow) {
		int cell = 0;
		createHeaderCell(sheet, currentRow, cell++, "Operadora");
		createHeaderCell(sheet, currentRow, cell++, "Local");
		createHeaderCell(sheet, currentRow, cell++, "Fecha");
		createHeaderCell(sheet, currentRow, cell++, "Liquidacion");
		createHeaderCell(sheet, currentRow, cell++, "Factura");
		createHeaderCell(sheet, currentRow, cell++, "Estado");
		createHeaderCell(sheet, currentRow, cell++, "Modelo");
		cell += 3;
		createHeaderCell(sheet, currentRow, cell++, "Fecha");
		createHeaderCell(sheet, currentRow, cell++, "Stakes");
		createHeaderCell(sheet, currentRow, cell++, "GGR");
		createHeaderCell(sheet, currentRow, cell++, "NGR");
		createHeaderCell(sheet, currentRow, cell++, "Gastos Operativos");
		createHeaderCell(sheet, currentRow, cell++, "NR");
		createHeaderCell(sheet, currentRow, cell++, "Store cash");
		return currentRow++;
	}

	private int createLiquidationDetails(HSSFSheet sheet, int currentRow, Liquidation liquidation) {
		for (int i = 0; i < liquidation.getBills().size(); i++) {
			Bill bill = liquidation.getBills().get(i);
			int cell = 0;
			createCell(sheet, currentRow, cell++, liquidation.getSender().getName());
			createCell(sheet, currentRow, cell++, bill.getSender().getName());
			createCell(sheet, currentRow, cell++, bill.getBillDate());
			createCell(sheet, currentRow, cell++, "");
			createCell(sheet, currentRow, cell++, "");
			createCell(sheet, currentRow, cell++, "");
			createCell(sheet, currentRow, cell++, "");
			cell += 3;
			createHeaderCell(sheet, currentRow, cell++, "");
			createHeaderCell(sheet, currentRow, cell++, "");
			createHeaderCell(sheet, currentRow, cell++, "");
			createHeaderCell(sheet, currentRow, cell++, "");
			createHeaderCell(sheet, currentRow, cell++, "");
			createHeaderCell(sheet, currentRow, cell++, "");
			createHeaderCell(sheet, currentRow, cell++, "");
			currentRow++;
		}
		return currentRow;
	}

}
