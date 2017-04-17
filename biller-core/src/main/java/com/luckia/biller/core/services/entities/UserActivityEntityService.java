package com.luckia.biller.core.services.entities;

import java.util.Date;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import com.luckia.biller.core.model.UserActivity;

public class UserActivityEntityService extends EntityService<UserActivity> {

	@Override
	protected Class<UserActivity> getEntityClass() {
		return UserActivity.class;
	}

	@Override
	protected void buildOrderCriteria(CriteriaQuery<UserActivity> criteria,
			CriteriaBuilder builder, Root<UserActivity> root) {
		criteria.orderBy(builder.desc(root.<Date>get("date")));
	}
}
