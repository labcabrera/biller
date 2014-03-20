package com.luckia.biller.core.services.bills;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.commons.lang3.Range;

import com.luckia.biller.core.model.Bill;
import com.luckia.biller.core.model.BillConcept;

/**
 * {@link BillDataProvider} que genera los resultados a partir de la tabla de LIS de registros de facturacion por terminal.
 */
public class DummyBillingDataProvider implements BillDataProvider {

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.luckia.biller.core.services.billing.BillingDataProvider#retreive(com.luckia.biller.core.model.Bill,
	 * org.apache.commons.lang3.Range, java.util.List)
	 */
	@Override
	public Map<BillConcept, BigDecimal> retreive(Bill bill, Range<Date> range, List<String> terminals) {
		Map<BillConcept, BigDecimal> result = new HashMap<BillConcept, BigDecimal>();

		for (BillConcept i : BillConcept.values()) {
			int max = 15000;
			int min = 200;
			int integer = new Random().nextInt(100 * (max - min));
			result.put(i, new BigDecimal(integer).divide(new BigDecimal(100), 2, RoundingMode.HALF_EVEN));
		}
		return result;
	}
}
