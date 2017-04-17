package com.luckia.biller.core.services.entities;

import java.util.Date;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import com.luckia.biller.core.model.Liquidation;

public class LiquidationEntityService extends EntityService<Liquidation> {

	@Override
	protected void buildOrderCriteria(CriteriaQuery<Liquidation> criteria,
			CriteriaBuilder builder, Root<Liquidation> root) {
		criteria.orderBy(builder.desc(root.<Date>get("billDate")),
				builder.asc(root.<String>get("sender").get("name")),
				builder.desc(root.<String>get("code")));
	}

	@Override
	protected Class<Liquidation> getEntityClass() {
		return Liquidation.class;
	}
}
