package com.luckia.biller.deploy.poi;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BaseWoorbookProcessor {

	private static final Logger LOG = LoggerFactory.getLogger(BaseWoorbookProcessor.class);

	protected Date readCellAsDate(Cell cell) {
		if (cell != null) {
			try {
				return cell.getDateCellValue();
			} catch (Exception ex) {
				ex.printStackTrace();
			}

			switch (cell.getCellType()) {
			case Cell.CELL_TYPE_STRING:
				try {
					return new SimpleDateFormat("dd/MM/yyyy").parse(cell.getStringCellValue());
				} catch (ParseException ex) {
					throw new RuntimeException("Unsupported format: " + cell.getStringCellValue() + ". Expected: dd/MM/yyyyy");
				}
			default:
				throw new RuntimeException("Unsupported cell type: " + cell.getCellType());
			}
		} else {
			return null;
		}
	}

	protected String readCellAsString(Cell cell) {
		return readCellAsString(cell, cell.getCellType());
	}

	protected String readCellAsString(Cell cell, int type) {
		if (cell == null) {
			return null;
		} else if (type == Cell.CELL_TYPE_NUMERIC) {
			return String.valueOf(cell.getNumericCellValue());
		} else if (type == Cell.CELL_TYPE_FORMULA) {
			FormulaEvaluator formulaEvaluator = cell.getRow().getSheet().getWorkbook().getCreationHelper().createFormulaEvaluator();
			int resultType = formulaEvaluator.evaluateFormulaCell(cell);
			return readCellAsString(cell, resultType);
		} else {
			return StringUtils.trim(cell.getStringCellValue());
		}
	}

	protected BigDecimal readCellAsBigDecimal(Cell cell) {
		return readCellAsBigDecimal(cell, cell.getCellType());
	}

	protected BigDecimal readCellAsBigDecimal(Cell cell, int type) {
		BigDecimal result = null;
		if (cell != null) {
			switch (type) {
			case Cell.CELL_TYPE_NUMERIC:
				try {
					result = new BigDecimal(cell.getNumericCellValue());
					break;
				} catch (Exception ex) {
					throw new RuntimeException("Invalid cell number value");
				}
			case Cell.CELL_TYPE_STRING:
				try {
					result = new BigDecimal(cell.getStringCellValue());
					break;
				} catch (Exception ex) {
					throw new RuntimeException("Unsupported number format: " + cell.getStringCellValue(), ex);
				}
			case Cell.CELL_TYPE_FORMULA:
				try {
					FormulaEvaluator formulaEvaluator = cell.getRow().getSheet().getWorkbook().getCreationHelper().createFormulaEvaluator();
					int resultType = formulaEvaluator.evaluateFormulaCell(cell);
					return readCellAsBigDecimal(cell, resultType);
				} catch (Exception ex) {
					throw new RuntimeException("Unsupported formula: " + cell.getStringCellValue(), ex);
				}
			default:
				throw new RuntimeException("Unsupported cell type: " + cell.getCellType());
			}
		}
		if (result != null && result.scale() > getAmountScale()) {
			LOG.warn("Invalid scale: " + result);
			result = result.setScale(getAmountScale(), RoundingMode.HALF_EVEN);
		}
		return result;
	}

	protected Integer getAmountScale() {
		return 2;
	}

}
