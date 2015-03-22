/*******************************************************************************
 * Copyright (c) 2004, 2012 Kotasoft S.L.
 * All rights reserved. This program and the accompanying materials
 * may only be used prior written consent of Kotasoft S.L.
 ******************************************************************************/
package com.luckia.biller.core.services.entities;

import java.util.Date;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import com.luckia.biller.core.model.Liquidation;

public class LiquidationEntityService extends EntityService<Liquidation> {

	@Override
	protected void buildOrderCriteria(CriteriaQuery<Liquidation> criteria, CriteriaBuilder builder, Root<Liquidation> root) {
		criteria.orderBy(builder.desc(root.<Date> get("billDate")), builder.asc(root.<String> get("sender").get("name")), builder.desc(root.<String> get("code")));
	}

	@Override
	protected Class<Liquidation> getEntityClass() {
		return Liquidation.class;
	}
}
