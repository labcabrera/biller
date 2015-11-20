package com.luckia.biller.core.services.entities;

import java.io.Serializable;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import org.apache.commons.lang3.StringUtils;

import com.luckia.biller.core.i18n.I18nService;
import com.luckia.biller.core.jpa.FiqlParser;
import com.luckia.biller.core.model.common.Message;
import com.luckia.biller.core.model.common.SearchParams;
import com.luckia.biller.core.model.common.SearchResults;
import com.luckia.biller.core.services.AuditService;

/**
 * Servicio que provee las funcionalidades basicas de JPA para diferentes entidades del modelo.
 * 
 * @param <I>
 */
public abstract class EntityService<I> {

	@Inject
	protected Provider<EntityManager> entityManagerProvider;
	@Inject
	protected Validator validator;
	@Inject
	protected I18nService i18nService;
	@Inject
	protected FiqlParser fiqlParser;
	@Inject
	protected AuditService auditService;

	protected abstract Class<I> getEntityClass();

	public I findById(Serializable primaryKey) {
		EntityManager entityManager = entityManagerProvider.get();
		entityManager.clear();
		return entityManager.find(getEntityClass(), primaryKey);
	}

	/**
	 * Genera los resultados a partir de la expression FIQL definida en el <code>SearchParams.queryString</code>.
	 * 
	 * @param params
	 * @return
	 */
	public SearchResults<I> find(SearchParams params) {
		Class<I> entityClass = getEntityClass();
		EntityManager entityManager = entityManagerProvider.get();
		CriteriaBuilder builder = entityManager.getCriteriaBuilder();
		CriteriaQuery<I> criteria = builder.createQuery(entityClass);
		Root<I> root = criteria.from(entityClass);
		Predicate predicate = StringUtils.isBlank(params.getQueryString()) ? builder.conjunction() : fiqlParser.parse(params.getQueryString(), builder, root).build();
		criteria.where(predicate);
		buildOrderCriteria(criteria, builder, root);
		TypedQuery<I> query = entityManager.createQuery(criteria);
		configureQueryRange(query, params);
		SearchResults<I> searchResults = new SearchResults<I>();
		searchResults.setResults(query.getResultList());
		// Numero de resultados
		CriteriaQuery<Long> criteriaCount = builder.createQuery(Long.class);
		criteriaCount.select(builder.count(root));
		criteriaCount.where(predicate);
		Long totalItems = entityManager.createQuery(criteriaCount).getSingleResult();
		Long totalPages = (totalItems + (totalItems % params.getItemsPerPage())) / params.getItemsPerPage();
		searchResults.setTotalItems(totalItems);
		searchResults.setTotalPages(totalPages);
		searchResults.setCurrentPage(params.getCurrentPage());
		searchResults.setItemsPerPage(params.getItemsPerPage());
		return searchResults;
	}

	public Message<I> validate(I entity) {
		Set<ConstraintViolation<I>> violations = validator.validate(entity);
		Message<I> message = new Message<I>();
		message.setPayload(entity);
		if (violations.isEmpty()) {
			message.setCode(Message.CODE_SUCCESS);
			message.setMessage("Entidad válida");
		} else {
			message.setCode(Message.CODE_GENERIC_ERROR);
			message.setMessage("Entidad con errores de validación");
			for (ConstraintViolation<I> violation : violations) {
				message.addError(i18nService.getMessage(violation.getMessage()));
			}
		}
		return message;
	}

	public Message<I> merge(I entity) {
		throw new RuntimeException("Not implemented");
	}

	public Message<I> persist(I entity) {
		throw new RuntimeException("Not implemented");
	}

	public Message<I> remove(Serializable primaryKey) {
		throw new RuntimeException("Not implemented");
	}

	/**
	 * Podemos sobreescribir este metodo para establecer la ordenacion de resultados en nuestras consultas.
	 * 
	 * @param criteria
	 * @param builder
	 * @param root
	 */
	protected void buildOrderCriteria(CriteriaQuery<I> criteria, CriteriaBuilder builder, Root<I> root) {
	}

	/**
	 * Podemos sobreescribir este metodo para establecer el numero de resultados por defecto.
	 * 
	 * @return
	 */
	protected Integer getDefaultsItemsPerPage() {
		return 10;
	}

	protected void configureQueryRange(Query query, SearchParams params) {
		if (params.getCurrentPage() == null || params.getCurrentPage() < 1) {
			params.setCurrentPage(1);
		}
		if (params.getItemsPerPage() == null || params.getItemsPerPage() < 1) {
			params.setItemsPerPage(getDefaultsItemsPerPage());
		}
		query.setMaxResults(params.getItemsPerPage());
		query.setFirstResult(params.getItemsPerPage() * (params.getCurrentPage() - 1));
	}

}
