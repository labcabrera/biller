package com.luckia.biller.core.reporting;

import java.math.BigDecimal;
import java.util.Date;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFDataFormat;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.util.HSSFColor;

public class BaseReport {

	private HSSFCellStyle headerStyle = null;
	private HSSFCellStyle currentDateStyle = null;
	private HSSFCellStyle currentNumberStyle = null;

	protected HSSFCell createCell(HSSFSheet sheet, int rowIndex, int cellIndex) {
		HSSFRow row = sheet.getRow(rowIndex);
		if (row == null) {
			row = sheet.createRow(rowIndex);
		}
		HSSFCell cell = row.getCell(cellIndex);
		if (cell == null) {
			cell = row.createCell(cellIndex);
		}
		return cell;
	}

	protected HSSFCell createCell(HSSFSheet sheet, int rowIndex, int cellIndex, String value) {
		HSSFCell cell = createCell(sheet, rowIndex, cellIndex);
		cell.setCellValue(value);
		return cell;
	}

	protected HSSFCell createCell(HSSFSheet sheet, int rowIndex, int cellIndex, BigDecimal value) {
		HSSFCell cell = createCell(sheet, rowIndex, cellIndex);
		cell.setCellValue(value != null ? value.doubleValue() : 0d);
		cell.setCellStyle(getNumericStyle(sheet));
		return cell;
	}

	protected HSSFCell createCell(HSSFSheet sheet, int rowIndex, int cellIndex, Date value) {
		HSSFCell cell = createCell(sheet, rowIndex, cellIndex);
		if (value != null) {
			cell.setCellValue(value);
			cell.setCellStyle(getDateStyle(sheet));
		}
		return cell;
	}

	protected HSSFCell createHeaderCell(HSSFSheet sheet, int rowIndex, int cellIndex, String value) {
		HSSFCell cell = createCell(sheet, rowIndex, cellIndex, value);
		cell.setCellStyle(getHeaderStyle(sheet));
		return cell;
	}

	private HSSFCellStyle getNumericStyle(HSSFSheet sheet) {
		if (currentNumberStyle == null) {
			HSSFDataFormat hssfDataFormat = sheet.getWorkbook().createDataFormat();
			currentNumberStyle = sheet.getWorkbook().createCellStyle();
			currentNumberStyle.setDataFormat(hssfDataFormat.getFormat("#,##0.00"));
		}
		return currentNumberStyle;
	}

	private HSSFCellStyle getDateStyle(HSSFSheet sheet) {
		if (currentDateStyle == null) {
			short format = sheet.getWorkbook().createDataFormat().getFormat("dd/MM/yyyy");
			currentDateStyle = sheet.getWorkbook().createCellStyle();
			currentDateStyle.setDataFormat(format);
		}
		return currentDateStyle;
	}

	private HSSFCellStyle getHeaderStyle(HSSFSheet sheet) {
		if (headerStyle == null) {
			headerStyle = sheet.getWorkbook().createCellStyle();
			headerStyle.setFillForegroundColor(HSSFColor.GOLD.index);
			headerStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
		}
		return headerStyle;
	}
}
