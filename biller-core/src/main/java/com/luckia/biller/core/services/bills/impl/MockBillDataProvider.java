package com.luckia.biller.core.services.bills.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.lang3.Range;

import com.luckia.biller.core.common.MathUtils;
import com.luckia.biller.core.model.Bill;
import com.luckia.biller.core.model.BillConcept;
import com.luckia.biller.core.services.bills.BillDataProvider;
import com.luckia.biller.core.services.entities.ProvinceTaxesService;

/**
 * Implementación de {@link BillDataProvider} que obtiene la información de la base de datos de LIS.
 */
public class MockBillDataProvider implements BillDataProvider {

	@Inject
	private ProvinceTaxesService provinceTaxesService;

	public Map<BillConcept, BigDecimal> retreive(Range<Date> range, List<String> terminals) {
		return retreive(null, range, terminals);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.luckia.biller.core.services.billing.BillingDataProvider#retreive(com.luckia.biller.core.model.Bill,
	 * org.apache.commons.lang3.Range, java.util.List)
	 */
	@Override
	public Map<BillConcept, BigDecimal> retreive(Bill bill, Range<Date> range, List<String> terminals) {
		BigDecimal totalBetAmount = new BigDecimal("1000");
		BigDecimal totalCancelledAmount = new BigDecimal("5");
		BigDecimal totalAttributable = new BigDecimal("490");
		BigDecimal coOperatingMonthlyFees = new BigDecimal("20");
		BigDecimal totalWinAmount = new BigDecimal("500");

		BigDecimal gameFeesPercent = bill != null ? provinceTaxesService.getGameFeesPercent(bill) : BigDecimal.ZERO;
		BigDecimal stakes = totalBetAmount.subtract(totalCancelledAmount);
		BigDecimal ggr = stakes.subtract(totalAttributable);
		BigDecimal tasaDeJuego = ggr.multiply(gameFeesPercent).divide(MathUtils.HUNDRED, 2, RoundingMode.HALF_EVEN);
		BigDecimal ngr = ggr.subtract(tasaDeJuego);
		BigDecimal gastosOperativos = coOperatingMonthlyFees;
		BigDecimal nr = ngr.subtract(gastosOperativos);
		BigDecimal storeCash = stakes.subtract(totalWinAmount);

		Map<BillConcept, BigDecimal> map = new HashMap<BillConcept, BigDecimal>();
		map.put(BillConcept.TOTAL_BET_AMOUNT, totalBetAmount);
		map.put(BillConcept.CANCELLED, totalCancelledAmount);
		map.put(BillConcept.TOTAL_WIN_AMOUNT, totalCancelledAmount);
		map.put(BillConcept.TOTAL_ATTRIBUTABLE, totalCancelledAmount);
		map.put(BillConcept.MARGIN, stakes.subtract(totalAttributable));
		map.put(BillConcept.STAKES, stakes);
		map.put(BillConcept.GGR, ggr);
		map.put(BillConcept.NGR, ngr);
		map.put(BillConcept.NR, nr);
		map.put(BillConcept.STORE_CASH, storeCash);
		return map;
	}

}
