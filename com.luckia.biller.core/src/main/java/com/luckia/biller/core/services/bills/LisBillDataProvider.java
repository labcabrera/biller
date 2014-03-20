package com.luckia.biller.core.services.bills;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.luckia.biller.core.Constants;
import com.luckia.biller.core.common.MathUtils;
import com.luckia.biller.core.jpa.EntityManagerProvider;
import com.luckia.biller.core.model.Bill;
import com.luckia.biller.core.model.BillConcept;
import com.luckia.biller.core.model.lis.LisTerminalRecord;

public class LisBillDataProvider implements BillDataProvider {

	private static final Logger LOG = LoggerFactory.getLogger(LisBillDataProvider.class);

	@Inject
	@Named(Constants.LIS)
	private EntityManagerProvider entityManagerProvider;

	@Inject
	private BillFeesService billFeesService;

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.luckia.biller.core.services.billing.BillingDataProvider#retreive(com.luckia.biller.core.model.Bill,
	 * org.apache.commons.lang3.Range, java.util.List)
	 */
	@Override
	public Map<BillConcept, BigDecimal> retreive(Bill bill, Range<Date> range, List<String> terminals) {
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
			for (LisTerminalRecord i : records) {
				totalBetAmount = totalBetAmount.add(i.getBetAmount());
				totalWinAmount = totalWinAmount.add(i.getWinAmount());
				totalCancelledAmount = totalCancelledAmount.add(i.getCancelledAmount());
			}
			BigDecimal gameFeesPercent = billFeesService.getGameFeesPercent(bill);
			BigDecimal stakes = totalBetAmount.subtract(totalCancelledAmount);
			BigDecimal ggr = stakes.subtract(totalWinAmount);
			BigDecimal tasaDeJuego = ggr.multiply(gameFeesPercent).divide(MathUtils.HUNDRED, 2, RoundingMode.HALF_EVEN);
			BigDecimal ngr = ggr.subtract(tasaDeJuego);
			BigDecimal gastosOperativos = bill.getModel().getCoOperatingMonthlyFees();
			BigDecimal nr = ngr.subtract(gastosOperativos);
			map.put(BillConcept.Stakes, stakes);
			map.put(BillConcept.GGR, ggr);
			map.put(BillConcept.NGR, ngr);
			map.put(BillConcept.NR, nr);
		}
		return map;
	}
}
