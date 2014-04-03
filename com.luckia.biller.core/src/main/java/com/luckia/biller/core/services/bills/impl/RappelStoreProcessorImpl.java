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

import com.luckia.biller.core.common.MathUtils;
import com.luckia.biller.core.jpa.EntityManagerProvider;
import com.luckia.biller.core.model.BillConcept;
import com.luckia.biller.core.model.BillState;
import com.luckia.biller.core.model.RappelStoreBonus;
import com.luckia.biller.core.model.Store;
import com.luckia.biller.core.model.TerminalRelation;
import com.luckia.biller.core.services.AuditService;
import com.luckia.biller.core.services.StateMachineService;
import com.luckia.biller.core.services.bills.BillDataProvider;
import com.luckia.biller.core.services.bills.RappelStoreProcessor;

public class RappelStoreProcessorImpl implements RappelStoreProcessor {

	@Inject
	private BillDataProvider billDataProvider;
	@Inject
	private EntityManagerProvider entityManagerProvider;
	@Inject
	private AuditService auditService;
	@Inject
	private StateMachineService stateMachineService;

	@Override
	public void processRappel(Store store, Range<Date> range) {
		List<String> terminals = new ArrayList<>();
		for (TerminalRelation terminalRelation : store.getTerminalRelations()) {
			terminals.add(terminalRelation.getCode());
		}
		Map<BillConcept, BigDecimal> billingData = billDataProvider.retreive(range, terminals);
		BigDecimal baseValue = billingData.get(BillConcept.Stakes);
		BigDecimal rappelBonus = getRappelBonusAmount(baseValue);
		if (MathUtils.isNotZeroPositive(rappelBonus)) {
			RappelStoreBonus bonus = new RappelStoreBonus();
			bonus.setId(UUID.randomUUID().toString());
			bonus.setStore(store);
			bonus.setBaseValue(baseValue);
			bonus.setValue(rappelBonus);
			bonus.setBonusDate(range.getMaximum());
			auditService.processCreated(bonus);
			stateMachineService.createTransition(bonus, BillState.BillDraft.name());
			EntityManager entityManager = entityManagerProvider.get();
			entityManager.getTransaction().begin();
			entityManager.persist(bonus);
			entityManager.getTransaction().commit();
		}
	}

	// TODO check rappel del store.billingModel
	public BigDecimal getRappelBonusAmount(BigDecimal baseValue) {
		return new BigDecimal("100");
	}
}
