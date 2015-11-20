package com.luckia.biller.web.rest;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;
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
import javax.ws.rs.core.MediaType;

import org.apache.commons.lang3.StringUtils;

import com.luckia.biller.core.model.Province;

@Path("/provinces")
public class ProvincesRestService {

	@Inject
	private Provider<EntityManager> entityManagerProvider;

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("id/{id}")
	public Province findById(@PathParam("id") String id) {
		return entityManagerProvider.get().find(Province.class, id);
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/find/{expression}")
	public List<Province> find(@PathParam("expression") String expression) {
		EntityManager entityManager = entityManagerProvider.get();
		CriteriaBuilder builder = entityManager.getCriteriaBuilder();
		CriteriaQuery<Province> criteria = builder.createQuery(Province.class);
		Root<Province> root = criteria.from(Province.class);
		if (StringUtils.isNotBlank(expression)) {
			Predicate predicate = builder.like(root.<String> get("name"), "%" + expression + "%");
			criteria.where(predicate);
		}
		criteria.orderBy(builder.asc(root.<String> get("name")));
		TypedQuery<Province> typedQuery = entityManager.createQuery(criteria);
		typedQuery.setMaxResults(15);
		return typedQuery.getResultList();
	}
}
