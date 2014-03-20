/*******************************************************************************
 * Copyright (c) 2004, 2012 Kotasoft S.L.
 * All rights reserved. This program and the accompanying materials
 * may only be used prior written consent of Kotasoft S.L.
 ******************************************************************************/
package com.luckia.biller.core.services.entities;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import com.luckia.biller.core.model.Bill;

public class BillEntityService extends EntityService<Bill> {

	@Override
	protected void buildOrderCriteria(CriteriaQuery<Bill> criteria, CriteriaBuilder builder, Root<Bill> root) {
		criteria.orderBy(builder.desc(root.<String> get("code")), builder.asc(root.<String> get("sender").get("name")));
	}

	@Override
	protected Class<Bill> getEntityClass() {
		return Bill.class;
	}
}
