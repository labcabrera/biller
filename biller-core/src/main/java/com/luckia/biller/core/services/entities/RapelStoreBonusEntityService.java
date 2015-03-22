package com.luckia.biller.core.services.entities;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import com.luckia.biller.core.model.RappelStoreBonus;
import com.luckia.biller.core.model.Store;

public class RapelStoreBonusEntityService extends EntityService<RappelStoreBonus> {

	@Override
	protected Class<RappelStoreBonus> getEntityClass() {
		return RappelStoreBonus.class;
	}

	@Override
	protected void buildOrderCriteria(CriteriaQuery<RappelStoreBonus> criteria, CriteriaBuilder builder, Root<RappelStoreBonus> root) {
		criteria.orderBy(builder.asc(root.<Store> get("store").get("name")));
	}
}
