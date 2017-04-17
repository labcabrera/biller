package com.luckia.biller.core.services.entities;

import java.util.Date;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import com.luckia.biller.core.model.Bill;

public class BillEntityService extends EntityService<Bill> {

	@Override
	protected void buildOrderCriteria(CriteriaQuery<Bill> criteria,
			CriteriaBuilder builder, Root<Bill> root) {
		criteria.orderBy(builder.desc(root.<Date>get("billDate")),
				builder.asc(root.<String>get("sender").get("name")),
				builder.desc(root.<String>get("code")));
	}

	@Override
	protected Class<Bill> getEntityClass() {
		return Bill.class;
	}
}
