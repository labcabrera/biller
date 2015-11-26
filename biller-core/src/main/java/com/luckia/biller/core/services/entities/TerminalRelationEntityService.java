package com.luckia.biller.core.services.entities;

import java.io.Serializable;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import com.google.inject.persist.Transactional;
import com.luckia.biller.core.common.RegisterActivity;
import com.luckia.biller.core.model.TerminalRelation;
import com.luckia.biller.core.model.UserActivityType;
import com.luckia.biller.core.model.common.Message;

/**
 * {@link EntityService} asociado a la entidad {@link TerminalRelation}
 */
public class TerminalRelationEntityService extends EntityService<TerminalRelation> {

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.luckia.biller.core.services.entities.EntityService#merge(java.lang.Object)
	 */
	@Override
	@Transactional
	@RegisterActivity(type = UserActivityType.TERMINAL_RELATION_MERGE)
	public Message<TerminalRelation> merge(TerminalRelation entity) {
		EntityManager entityManager = entityManagerProvider.get();
		TerminalRelation current;
		String message;
		if (entity.getId() == null) {
			current = new TerminalRelation();
			current.merge(entity);
			auditService.processCreated(current);
			entityManager.persist(current);
			message = i18nService.getMessage("terminal.persist");
		} else {
			current = entityManager.find(TerminalRelation.class, entity.getId());
			current.merge(entity);
			auditService.processModified(current);
			current = entityManager.merge(current);
			message = i18nService.getMessage("terminal.merge");
		}
		return new Message<>(Message.CODE_SUCCESS, message, current);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.luckia.biller.core.services.entities.EntityService#remove(java.io.Serializable)
	 */
	@Override
	@Transactional
	@RegisterActivity(type = UserActivityType.TERMINAL_RELATION_REMOVE)
	public Message<TerminalRelation> remove(Serializable primaryKey) {
		EntityManager entityManager = entityManagerProvider.get();
		TerminalRelation current = entityManager.find(TerminalRelation.class, primaryKey);
		auditService.processDeleted(current);
		entityManager.merge(current);
		return new Message<>(Message.CODE_SUCCESS, i18nService.getMessage("terminal.remove"), current);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.luckia.biller.core.services.entities.EntityService#getEntityClass()
	 */
	@Override
	protected Class<TerminalRelation> getEntityClass() {
		return TerminalRelation.class;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.luckia.biller.core.services.entities.EntityService#buildOrderCriteria(javax.persistence.criteria.CriteriaQuery, javax.persistence.criteria.CriteriaBuilder,
	 * javax.persistence.criteria.Root)
	 */
	@Override
	protected void buildOrderCriteria(CriteriaQuery<TerminalRelation> criteria, CriteriaBuilder builder, Root<TerminalRelation> root) {
		criteria.orderBy(builder.asc(root.<String> get("code")));
	}
}
