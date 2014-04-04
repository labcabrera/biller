/*******************************************************************************
 * Copyright (c) 2004, 2012 Kotasoft S.L.
 * All rights reserved. This program and the accompanying materials
 * may only be used prior written consent of Kotasoft S.L.
 ******************************************************************************/
package com.luckia.biller.web.rest;

import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.apache.commons.lang3.StringUtils;

import com.luckia.biller.core.jpa.EntityManagerProvider;
import com.luckia.biller.core.model.Province;
import com.luckia.biller.core.model.Region;

@Path("regions")
public class RegionsRestService {

	@Inject
	private EntityManagerProvider entityManagerProvider;

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("id/{id}")
	public Region findById(@PathParam("id") String id) {
		return entityManagerProvider.get().find(Region.class, id);
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/find/{expression}")
	public List<Region> find(@PathParam("expression") String expression, @QueryParam("province") Long province) {
		EntityManager entityManager = entityManagerProvider.get();
		CriteriaBuilder builder = entityManager.getCriteriaBuilder();
		CriteriaQuery<Region> criteria = builder.createQuery(Region.class);
		Root<Region> root = criteria.from(Region.class);
		Predicate predicate = builder.conjunction();
		if (StringUtils.isNotBlank(expression)) {
			predicate = builder.and(predicate, builder.like(root.<String> get("name"), "%" + expression + "%"));
		}
		if (province != null) {
			predicate = builder.and(predicate, builder.equal(root.<Province> get("province").get("id"), province));
		}
		criteria.where(predicate);
		criteria.orderBy(builder.asc(root.<String> get("name")));
		TypedQuery<Region> typedQuery = entityManager.createQuery(criteria);
		typedQuery.setMaxResults(15);
		return typedQuery.getResultList();
	}
}
