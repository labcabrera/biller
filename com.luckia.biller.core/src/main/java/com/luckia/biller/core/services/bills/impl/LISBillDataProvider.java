package com.luckia.biller.core.services.bills.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.apache.commons.lang3.Range;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.luckia.biller.core.Constants;
import com.luckia.biller.core.common.MathUtils;
import com.luckia.biller.core.jpa.EntityManagerProvider;
import com.luckia.biller.core.model.Bill;
import com.luckia.biller.core.model.BillConcept;
import com.luckia.biller.core.model.lis.LisTerminalRecord;
import com.luckia.biller.core.services.bills.BillDataProvider;
import com.luckia.biller.core.services.bills.BillFeesService;

/**
 * Implementación de {@link BillDataProvider} que obtiene la información de la base de datos de LIS.
 */
public class LISBillDataProvider implements BillDataProvider {

	private static final Logger LOG = LoggerFactory.getLogger(LISBillDataProvider.class);

	@Inject
	@Named(Constants.LIS)
	private EntityManagerProvider entityManagerProvider;
	@Inject
	private BillFeesService billFeesService;

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
		Validate.notNull(terminals, "No se ha establecido la lista de terminales");
		Validate.notEmpty(terminals, "La lista de terminales no puede estar vacia");
		Map<BillConcept, BigDecimal> map = new HashMap<BillConcept, BigDecimal>();
		EntityManager entityManager = entityManagerProvider.get();
		TypedQuery<LisTerminalRecord> query = entityManager.createNamedQuery("LisTerminalRecord.selectByCodesInRange", LisTerminalRecord.class);
		query.setParameter("codes", terminals);
		query.setParameter("from", range.getMinimum());
		query.setParameter("to", range.getMaximum());
		List<LisTerminalRecord> records = query.getResultList();
		if (records.isEmpty()) {
			LOG.info("No hay registros de facturacion para los terminales {}", Arrays.toString(terminals.toArray()));
		} else {
			BigDecimal totalBetAmount = BigDecimal.ZERO;
			BigDecimal totalWinAmount = BigDecimal.ZERO;
			BigDecimal totalCancelledAmount = BigDecimal.ZERO;
			BigDecimal totalAttributable = BigDecimal.ZERO;
			for (LisTerminalRecord i : records) {
				totalBetAmount = totalBetAmount.add(i.getBetAmount());
				totalWinAmount = totalWinAmount.add(i.getWinAmount());
				totalCancelledAmount = totalCancelledAmount.add(i.getCancelledAmount());
				totalAttributable = totalAttributable.add(i.getAttributable());
			}
			BigDecimal gameFeesPercent = bill != null ? billFeesService.getGameFeesPercent(bill) : BigDecimal.ZERO;
			BigDecimal stakes = totalBetAmount.subtract(totalCancelledAmount);
			BigDecimal ggr = stakes.subtract(totalAttributable);
			BigDecimal tasaDeJuego = ggr.multiply(gameFeesPercent).divide(MathUtils.HUNDRED, 2, RoundingMode.HALF_EVEN);
			BigDecimal ngr = ggr.subtract(tasaDeJuego);
			BigDecimal gastosOperativos = bill != null ? bill.getModel().getCompanyModel().getCoOperatingMonthlyFees() : BigDecimal.ZERO;
			BigDecimal nr = ngr.subtract(gastosOperativos);
			BigDecimal storeCash = stakes.subtract(totalWinAmount);
			map.put(BillConcept.Stakes, stakes);
			map.put(BillConcept.GGR, ggr);
			map.put(BillConcept.NGR, ngr);
			map.put(BillConcept.NR, nr);
			map.put(BillConcept.StoreCash, storeCash);
		}
		return map;
	}

}
