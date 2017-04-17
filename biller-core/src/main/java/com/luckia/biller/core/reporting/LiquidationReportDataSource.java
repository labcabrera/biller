package com.luckia.biller.core.reporting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import com.luckia.biller.core.model.CommonState;
import com.luckia.biller.core.model.CompanyGroup;
import com.luckia.biller.core.model.CostCenter;
import com.luckia.biller.core.model.LegalEntity;
import com.luckia.biller.core.model.Liquidation;

public class LiquidationReportDataSource {

	@Inject
	private Provider<EntityManager> entityManagerProvider;

	public Map<LegalEntity, List<Liquidation>> groupByCompany(List<Liquidation> list) {
		Map<LegalEntity, List<Liquidation>> result = new LinkedHashMap<>();
		for (Liquidation liquidation : list) {
			LegalEntity sender = liquidation.getSender();
			if (result.containsKey(sender)) {
				result.get(sender).add(liquidation);
			}
			else {
				List<Liquidation> tmp = new ArrayList<>();
				tmp.add(liquidation);
				result.put(sender, tmp);
			}
		}
		return result;
	}

	public List<Liquidation> findLiquidations(Date from, Date to, LegalEntity company,
			CostCenter costCenter, CompanyGroup companyGroup) {
		EntityManager entityManager = entityManagerProvider.get();
		if (costCenter == null) {
			CriteriaBuilder builder = entityManager.getCriteriaBuilder();
			CriteriaQuery<Liquidation> criteria = builder.createQuery(Liquidation.class);
			Root<Liquidation> root = criteria.from(Liquidation.class);
			List<Predicate> predicates = new ArrayList<>();
			// TODO state filter
			if (from != null) {
				predicates.add(
						builder.greaterThanOrEqualTo(root.<Date>get("billDate"), from));
			}
			if (to != null) {
				predicates.add(builder.lessThanOrEqualTo(root.<Date>get("billDate"), to));
			}
			if (company != null) {
				predicates.add(builder.equal(root.get("sender"), company));
			}
			if (companyGroup != null) {
				predicates.add(
						builder.equal(root.get("sender").get("parent"), companyGroup));
			}
			criteria.where(predicates.toArray(new Predicate[predicates.size()]));
			criteria.orderBy( //
					builder.asc(root.<Date>get("billDate")), //
					builder.asc(root.<String>get("sender").get("name")), //
					builder.desc(root.<String>get("code")));
			TypedQuery<Liquidation> query = entityManager.createQuery(criteria);
			return query.getResultList();
		}
		else {
			// TODO hard to use criteria api here due cost center relationship
			StringBuilder sb = new StringBuilder();
			sb.append("select distinct(l) from Liquidation l ");
			sb.append("join Bill b on b.liquidation =  l ");
			sb.append("join Store s on b.sender = s ");
			sb.append("where l.currentState.stateDefinition.id in :states ");
			sb.append("and l.billDate >= :from ");
			sb.append("and l.billDate <= :to ");
			sb.append("and s.costCenter.id = :costCenterId ");
			sb.append(company == null ? "" : "and l.sender.id = :companyId ");
			sb.append(companyGroup == null ? ""
					: "and l.sender.parent.id = :companyGroupId ");
			sb.append("order by l.billDate desc, l.id");
			TypedQuery<Liquidation> query = entityManager.createQuery(sb.toString(),
					Liquidation.class);
			query.setParameter("states",
					Arrays.asList(CommonState.CONFIRMED, CommonState.SENT));
			query.setParameter("from", from);
			query.setParameter("to", to);
			if (company != null) {
				query.setParameter("companyId", company.getId());
			}
			if (companyGroup != null) {
				query.setParameter("companyGroupId", companyGroup.getId());
			}
			query.setParameter("costCenterId", costCenter.getId());
			return query.getResultList();
		}
	}
}
