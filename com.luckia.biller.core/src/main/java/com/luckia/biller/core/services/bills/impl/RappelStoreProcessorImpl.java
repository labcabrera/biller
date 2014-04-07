package com.luckia.biller.core.services.bills.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.inject.Inject;
import javax.persistence.EntityManager;

import org.apache.commons.lang3.Range;

import com.luckia.biller.core.jpa.EntityManagerProvider;
import com.luckia.biller.core.model.BillConcept;
import com.luckia.biller.core.model.BillingModel;
import com.luckia.biller.core.model.CommonState;
import com.luckia.biller.core.model.Rappel;
import com.luckia.biller.core.model.RappelStoreBonus;
import com.luckia.biller.core.model.Store;
import com.luckia.biller.core.model.TerminalRelation;
import com.luckia.biller.core.services.AuditService;
import com.luckia.biller.core.services.StateMachineService;
import com.luckia.biller.core.services.bills.BillDataProvider;
import com.luckia.biller.core.services.bills.RappelStoreProcessor;

/**
 * Implementación de {@link RappelStoreProcessor}
 */
public class RappelStoreProcessorImpl implements RappelStoreProcessor {

	@Inject
	private BillDataProvider billDataProvider;
	@Inject
	private EntityManagerProvider entityManagerProvider;
	@Inject
	private AuditService auditService;
	@Inject
	private StateMachineService stateMachineService;

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.luckia.biller.core.services.bills.RappelStoreProcessor#processRappel(com.luckia.biller.core.model.Store,
	 * org.apache.commons.lang3.Range)
	 */
	@Override
	public void processRappel(Store store, Range<Date> range) {
		try {
			List<String> terminals = new ArrayList<>();
			for (TerminalRelation terminalRelation : store.getTerminalRelations()) {
				terminals.add(terminalRelation.getCode());
			}
			Map<BillConcept, BigDecimal> billingData = billDataProvider.retreive(range, terminals);
			BigDecimal baseValue = billingData.get(BillConcept.Stakes);
			Rappel rappel = getRappelBonusAmount(store, baseValue, BigDecimal.ZERO);
			RappelStoreBonus bonus = new RappelStoreBonus();
			bonus.setId(UUID.randomUUID().toString());
			bonus.setStore(store);
			bonus.setBaseValue(baseValue);
			bonus.setValue(rappel != null ? rappel.getBonusAmount() : BigDecimal.ZERO);
			bonus.setRappel(rappel);
			bonus.setBonusDate(range.getMaximum());
			EntityManager entityManager = entityManagerProvider.get();
			entityManager.getTransaction().begin();
			auditService.processCreated(bonus);
			stateMachineService.createTransition(bonus, CommonState.Draft.name());
			entityManager.persist(bonus);
			entityManager.getTransaction().commit();
		} catch (Exception ex) {
			throw new RuntimeException("Error al calcular el rappel del establecimiento " + store.getName(), ex);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.luckia.biller.core.services.bills.RappelStoreProcessor#applyProrata(com.luckia.biller.core.model.RappelStoreBonus,
	 * java.math.BigDecimal)
	 */
	@Override
	public void applyProrata(RappelStoreBonus bonus, BigDecimal prorata) {
		throw new RuntimeException("Not implemented");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.luckia.biller.core.services.bills.RappelStoreProcessor#confirm(com.luckia.biller.core.model.RappelStoreBonus)
	 */
	@Override
	public void confirm(RappelStoreBonus bonus) {
		EntityManager entityManager = entityManagerProvider.get();
		entityManager.getTransaction().begin();
		stateMachineService.createTransition(bonus, CommonState.Confirmed.name());
		entityManager.persist(bonus);
		entityManager.getTransaction().commit();
	}

	/**
	 * 
	 * @param store
	 * @param baseValue
	 * @param prorata
	 * @return
	 */
	public Rappel getRappelBonusAmount(Store store, BigDecimal baseValue, BigDecimal prorata) {
		// TODO aplicar prorateo
		BillingModel billingModel = store.getBillingModel();
		Rappel bestRappel = null;
		if (billingModel.getRappel() != null) {
			for (Rappel rappel : billingModel.getRappel()) {
				boolean checkRappel = rappel.getAmount().compareTo(baseValue) <= 0;
				boolean checkBest = bestRappel == null || rappel.getBonusAmount().compareTo(rappel.getBonusAmount()) <= 0;
				if (checkRappel && checkBest) {
					bestRappel = rappel;
				}
			}
		}
		return bestRappel;
	}
}
