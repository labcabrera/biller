package com.luckia.biller.core.reporting;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import com.luckia.biller.core.model.Company;
import com.luckia.biller.core.model.LegalEntity;
import com.luckia.biller.core.model.Liquidation;

public class LiquidationReportDataSource {

	@Inject
	private Provider<EntityManager> entityManagerProvider;

	public Map<LegalEntity, List<Liquidation>> getLiquidations(Date from, Date to, List<Company> entities) {
		EntityManager entityManager = entityManagerProvider.get();
		String qlString = "select e from Liquidation e where e.sender = :sender and e.dateFrom >= :from and e.dateTo <= :to order by e.dateFrom";
		TypedQuery<Liquidation> query = entityManager.createQuery(qlString, Liquidation.class);
		query.setParameter("from", from);
		query.setParameter("to", to);
		Map<LegalEntity, List<Liquidation>> result = new LinkedHashMap<>();
		for (LegalEntity entity : entities) {
			query.setParameter("sender", entity);
			List<Liquidation> liquidations = query.getResultList();
			if (!liquidations.isEmpty()) {
				result.put(entity, liquidations);
			}
		}
		return result;
	}
}
