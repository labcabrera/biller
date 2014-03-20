package com.luckia.biller.core.services.bills.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.apache.commons.lang3.Range;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.luckia.biller.core.jpa.EntityManagerProvider;
import com.luckia.biller.core.model.Bill;
import com.luckia.biller.core.model.BillState;
import com.luckia.biller.core.model.Company;
import com.luckia.biller.core.model.CostCenter;
import com.luckia.biller.core.model.Liquidation;
import com.luckia.biller.core.model.Store;
import com.luckia.biller.core.services.StateMachineService;
import com.luckia.biller.core.services.bills.LiquidationProcessor;

public class LiquidationProcessorImpl implements LiquidationProcessor {

	private static final Logger LOG = LoggerFactory.getLogger(LiquidationProcessorImpl.class);

	@Inject
	private EntityManagerProvider entityManagerProvider;
	@Inject
	private StateMachineService stateMachineService;

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.luckia.biller.core.services.bills.impl.LiquidationProcessor#process(com.luckia.biller.core.model.Company,
	 * org.apache.commons.lang3.Range)
	 */
	@Override
	public List<Liquidation> process(Company company, Range<Date> range) {
		EntityManager entityManager = entityManagerProvider.get();
		TypedQuery<Bill> query = entityManager.createNamedQuery("Bill.selectPendingByReceiverInRange", Bill.class);
		query.setParameter("receiver", company);
		query.setParameter("from", range.getMinimum());
		query.setParameter("to", range.getMaximum());
		List<Bill> bills = query.getResultList();
		Map<CostCenter, List<Bill>> costCenterMap = getCostCenterMapping(bills);
		if (!entityManager.getTransaction().isActive()) {
			entityManager.getTransaction().begin();
		}
		List<Liquidation> result = new ArrayList<Liquidation>();
		LOG.info("Centros de coste asociados a la liquidacion:");
		for (CostCenter costCenter : costCenterMap.keySet()) {
			LOG.info("Generando liquidacion asociada al centro de coste {}", costCenter.getName());
			List<Bill> billList = costCenterMap.get(costCenter);

			Liquidation liquidation = new Liquidation();
			liquidation.setId(UUID.randomUUID().toString());
			liquidation.setSender(company);
			liquidation.setReceiver(costCenter);
			liquidation.setBills(billList);
			liquidation.setDateFrom(range.getMinimum());
			liquidation.setDateTo(range.getMaximum());
			liquidation.setBillDate(range.getMaximum());
			liquidation.setModel(company.getBillingModel());

			BigDecimal totalAmount = BigDecimal.ZERO;
			for (Bill bill : billList) {
				totalAmount = totalAmount.add(bill.getAmount());
				bill.setLiquidation(liquidation);
				entityManager.merge(bill);
			}
			liquidation.setAmount(totalAmount);
			entityManager.persist(liquidation);

			result.add(liquidation);
			stateMachineService.createTransition(liquidation, BillState.BillDraft.name());
		}
		entityManager.getTransaction().commit();
		return result;
	}

	private Map<CostCenter, List<Bill>> getCostCenterMapping(List<Bill> bills) {
		Map<CostCenter, List<Bill>> costCenterMap = new LinkedHashMap<CostCenter, List<Bill>>();
		for (Bill bill : bills) {
			if (!BillState.BillEmpty.equals(bill.getCurrentState().getStateDefinition().getId())) {
				Store store = bill.getSender(Store.class);
				CostCenter costCenter = store.getCostCenter();
				if (costCenter == null) {
					LOG.warn("El local {} de la factura {} carece de centro de coste. No se puede realizar la liquidacion", store, bill);
				} else {
					if (!costCenterMap.containsKey(store.getCostCenter())) {
						costCenterMap.put(costCenter, new ArrayList<Bill>());
					}
					costCenterMap.get(costCenter).add(bill);
				}
			}
		}
		return costCenterMap;
	}
}
