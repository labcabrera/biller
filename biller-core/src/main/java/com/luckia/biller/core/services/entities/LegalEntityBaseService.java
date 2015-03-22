package com.luckia.biller.core.services.entities;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import com.luckia.biller.core.model.LegalEntity;

public abstract class LegalEntityBaseService<I extends LegalEntity> extends EntityService<I> {

	@Override
	protected void buildOrderCriteria(CriteriaQuery<I> criteria, CriteriaBuilder builder, Root<I> root) {
		criteria.orderBy(builder.asc(root.<String> get("name")));
	}
}
