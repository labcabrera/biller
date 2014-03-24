package com.luckia.biller.deploy.poi;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.luckia.biller.core.common.MathUtils;
import com.luckia.biller.core.jpa.EntityManagerProvider;
import com.luckia.biller.core.model.BillingModel;
import com.luckia.biller.core.model.BillingModelType;

public class BillingModelResolver extends BaseWoorbookProcessor {

	private static final Logger LOG = LoggerFactory.getLogger(BillingModelResolver.class);

	private List<BillingModel> existingModels;
	private List<BillingModel> newModels;

	@Inject
	public BillingModelResolver(EntityManagerProvider entityManagerProvider) {
		EntityManager entityManager = entityManagerProvider.get();
		TypedQuery<BillingModel> query = entityManager.createNamedQuery("BillingModel.selectAll", BillingModel.class);
		existingModels = query.getResultList();
		newModels = new ArrayList<BillingModel>();
	}

	// TODO no esta teniendo en cuenta el rapel anual
	public BillingModel resolveBillingModel(Row row) {
		BillingModel current = new BillingModel();
		current.setStakesPercentStore(parseBigDecimal(row.getCell(13), 4).multiply(MathUtils.HUNDRED).setScale(2, RoundingMode.HALF_EVEN));
		current.setStakesPercentOperator(parseBigDecimal(row.getCell(14), 4).multiply(MathUtils.HUNDRED).setScale(2, RoundingMode.HALF_EVEN));
		current.setGgrPercent(parseBigDecimal(row.getCell(15), 2).multiply(MathUtils.HUNDRED).setScale(2, RoundingMode.HALF_EVEN));
		current.setNgrPercent(parseBigDecimal(row.getCell(16), 2).multiply(MathUtils.HUNDRED).setScale(2, RoundingMode.HALF_EVEN));
		current.setCoOperatingMonthlyFees(parseBigDecimal(row.getCell(18), 2));
		current.setCommercialMonthlyFees(parseBigDecimal(row.getCell(17), 2));
		current.setSatMonthlyFees(parseBigDecimal(row.getCell(19), 2));
		current.setName(readCellAsString(row.getCell(11)));
		current.setType(BillingModelType.Bill);
		BillingModel result = null;
		ModelComparator modelComparator = new ModelComparator();
		// Buscamos en los modelos existentes previamente
		for (BillingModel i : existingModels) {
			if (modelComparator.compare(current, i) == 0) {
				result = i;
			}
		}
		// Buscamos en los modelos generados durante la importacion
		if (result == null) {
			for (BillingModel i : newModels) {
				if (modelComparator.compare(current, i) == 0) {
					result = i;
				}
			}
		}
		// En caso de no existir lo creamos
		if (result == null) {
			LOG.info("Registrando nuevo modelo {}", current);
			result = current;
			newModels.add(current);
		}
		return result;
	}

	private BigDecimal parseBigDecimal(Cell cell, int scale) {
		try {
			return new BigDecimal(readCellAsString(cell)).setScale(scale, RoundingMode.HALF_EVEN);
		} catch (Exception ex) {
			return null;
		}
	}

	private static final class ModelComparator implements Comparator<BillingModel> {

		@Override
		public int compare(BillingModel first, BillingModel second) {
			boolean matches = true;
			matches &= compare(first.getCommercialMonthlyFees(), second.getCommercialMonthlyFees());
			matches &= compare(first.getCoOperatingMonthlyFees(), second.getCoOperatingMonthlyFees());
			matches &= compare(first.getGgrPercent(), second.getGgrPercent());
			matches &= compare(first.getNgrPercent(), second.getNgrPercent());
			matches &= compare(first.getNrPercent(), second.getNrPercent());
			matches &= compare(first.getSatMonthlyFees(), second.getSatMonthlyFees());
			matches &= compare(first.getStakesPercentStore(), second.getStakesPercentStore());
			matches &= compare(first.getStakesPercentOperator(), second.getStakesPercentOperator());
			return matches ? 0 : 1;
		}

		private boolean compare(BigDecimal a, BigDecimal b) {
			boolean checkA = a != null && BigDecimal.ZERO.compareTo(a) != 0;
			boolean checkB = b != null && BigDecimal.ZERO.compareTo(b) != 0;
			if (checkA && checkB) {
				return a.compareTo(b) == 0;
			} else if (!checkA && !checkB) {
				return true;
			} else {
				return false;
			}
		}
	}

	public List<BillingModel> getNewModels() {
		return newModels;
	}

	public List<BillingModel> getExistingModels() {
		return existingModels;
	}
}
