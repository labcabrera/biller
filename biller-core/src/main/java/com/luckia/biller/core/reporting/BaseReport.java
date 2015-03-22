package com.luckia.biller.core.reporting;

import java.util.Date;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFDataFormat;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.util.HSSFColor;

public class BaseReport {

	private static final String DATE_PATTERN = "m/d/yy";

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

	protected HSSFCell createCell(HSSFSheet sheet, int rowIndex, int cellIndex, Date value) {
		HSSFCell cell = createCell(sheet, rowIndex, cellIndex);
		if (value != null) {
			cell.setCellValue(value);
			HSSFCellStyle cellStyle = sheet.getWorkbook().createCellStyle();
			cellStyle.setDataFormat(HSSFDataFormat.getBuiltinFormat(DATE_PATTERN));
			cell.setCellStyle(cellStyle);
		}
		return cell;
	}

	protected HSSFCell createHeaderCell(HSSFSheet sheet, int rowIndex, int cellIndex, String value) {
		HSSFCell cell = createCell(sheet, rowIndex, cellIndex, value);
		HSSFCellStyle cellStyle = sheet.getWorkbook().createCellStyle();
		cellStyle.setFillForegroundColor(HSSFColor.GOLD.index);
		cellStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
		cell.setCellStyle(cellStyle);
		return cell;
	}
}
