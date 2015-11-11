package com.luckia.biller.core.reporting;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFDataFormat;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Clase base con metodos para facilitar la generacion de reports usando POI (estilos, creacion de celdas, etc).
 */
public abstract class BaseReport {

	private enum ReportStyle {
		DEFAULT, DEFAULT_DATE, DEFAULT_NUMBERIC, HEADER, HEADER_NUMBERIC, DISABLED, DISABLED_DATE, DISABLED_NUMERIC
	}

	private static final Logger LOG = LoggerFactory.getLogger(BaseReport.class);

	private Map<ReportStyle, HSSFCellStyle> styles;

	// e7501e
	public void init(HSSFWorkbook workbook) {
		styles = new HashMap<>();
		styles.put(ReportStyle.DEFAULT, buildStyle(workbook));
		styles.put(ReportStyle.DEFAULT_DATE, buildDateStyle(workbook, null));
		styles.put(ReportStyle.DEFAULT_NUMBERIC, buildNumericStyle(workbook, null));
		styles.put(ReportStyle.HEADER, buildStyle(workbook, HSSFColor.LIGHT_ORANGE.index));
		styles.put(ReportStyle.HEADER, buildNumericStyle(workbook, HSSFColor.LIGHT_ORANGE.index));
		styles.put(ReportStyle.HEADER_NUMBERIC, buildStyle(workbook, HSSFColor.LIGHT_ORANGE.index));
		styles.put(ReportStyle.DISABLED, buildStyle(workbook, HSSFColor.GREY_25_PERCENT.index));
		styles.put(ReportStyle.DISABLED_DATE, buildDateStyle(workbook, HSSFColor.GREY_25_PERCENT.index));
		styles.put(ReportStyle.DISABLED_NUMERIC, buildNumericStyle(workbook, HSSFColor.GREY_25_PERCENT.index));
	}

	protected HSSFCell createCell(HSSFSheet sheet, int rowIndex, int cellIndex, String value) {
		return applyStyle(createCellInternal(sheet, rowIndex, cellIndex, value), ReportStyle.DEFAULT);
	}

	protected HSSFCell createCell(HSSFSheet sheet, int rowIndex, int cellIndex, BigDecimal value) {
		return applyStyle(createCellInternal(sheet, rowIndex, cellIndex, value), ReportStyle.DEFAULT_NUMBERIC);
	}

	protected HSSFCell createCell(HSSFSheet sheet, int rowIndex, int cellIndex, Date value) {
		HSSFCell cell = createCellInternal(sheet, rowIndex, cellIndex, value);
		if (value != null) {
			return applyStyle(cell, ReportStyle.DEFAULT_DATE);
		}
		return cell;
	}

	protected HSSFCell createHeaderCell(HSSFSheet sheet, int rowIndex, int cellIndex, String value) {
		return applyStyle(createCellInternal(sheet, rowIndex, cellIndex, value), ReportStyle.HEADER);
	}

	protected HSSFCell createHeaderCell(HSSFSheet sheet, int rowIndex, int cellIndex, BigDecimal value) {
		return applyStyle(createCellInternal(sheet, rowIndex, cellIndex, value), ReportStyle.HEADER);
	}

	protected HSSFCell createDisabledCell(HSSFSheet sheet, int rowIndex, int cellIndex, String value) {
		return applyStyle(createCellInternal(sheet, rowIndex, cellIndex, value), ReportStyle.DISABLED);
	}

	protected HSSFCell createDisabledCell(HSSFSheet sheet, int rowIndex, int cellIndex, Date value) {
		return applyStyle(createCellInternal(sheet, rowIndex, cellIndex, value), ReportStyle.DISABLED_DATE);
	}

	protected HSSFCell createDisabledCell(HSSFSheet sheet, int rowIndex, int cellIndex, BigDecimal value) {
		return applyStyle(createCellInternal(sheet, rowIndex, cellIndex, value), ReportStyle.DISABLED_NUMERIC);
	}

	protected HSSFCell createCellInternal(HSSFSheet sheet, int rowIndex, int cellIndex, Object value) {
		HSSFRow row = sheet.getRow(rowIndex);
		if (row == null) {
			row = sheet.createRow(rowIndex);
		}
		HSSFCell cell = row.getCell(cellIndex);
		if (cell == null) {
			cell = row.createCell(cellIndex);
		}
		if (value != null) {
			if (value instanceof String) {
				cell.setCellValue((String) value);
			} else if (value instanceof Date) {
				cell.setCellValue((Date) value);
			} else if (value instanceof BigDecimal) {
				cell.setCellValue(((BigDecimal) value).doubleValue());
			} else {
				LOG.warn("Invalid type {}", value);
			}
		} else {
			cell.setCellValue(0d);
		}
		return applyStyle(cell, ReportStyle.DEFAULT);
	}

	private HSSFCell applyStyle(HSSFCell cell, ReportStyle reportStyle) {
		if (cell != null && styles.containsKey(reportStyle)) {
			cell.setCellStyle(styles.get(reportStyle));
		} else {
			LOG.warn("Invalid style {}", reportStyle);
		}
		return cell;
	}

	private HSSFCellStyle buildStyle(HSSFWorkbook workbook) {
		return buildStyle(workbook, null);
	}

	private HSSFCellStyle buildStyle(HSSFWorkbook workbook, Short color) {
		HSSFCellStyle style = workbook.createCellStyle();
		if (color != null) {
			style.setFillForegroundColor(color);
			style.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
		}
		return style;
	}

	private HSSFCellStyle buildNumericStyle(HSSFWorkbook workbook, Short color) {
		HSSFDataFormat hssfDataFormat = workbook.createDataFormat();
		HSSFCellStyle style = workbook.createCellStyle();
		style.setDataFormat(hssfDataFormat.getFormat("#,##0.00"));
		if (color != null) {
			style.setFillForegroundColor(color);
			style.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
		}
		return style;
	}

	private HSSFCellStyle buildDateStyle(HSSFWorkbook workbook, Short color) {
		HSSFCellStyle style = workbook.createCellStyle();
		short format = workbook.createDataFormat().getFormat("dd/MM/yyyy");
		style = workbook.createCellStyle();
		style.setDataFormat(format);
		if (color != null) {
			style.setFillForegroundColor(color);
			style.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
		}
		return style;
	}
}
