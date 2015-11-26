package com.luckia.biller.core.services.entities;

import java.io.Serializable;

import javax.inject.Inject;
import javax.persistence.EntityManager;

import com.google.inject.persist.Transactional;
import com.luckia.biller.core.common.RegisterActivity;
import com.luckia.biller.core.model.CostCenter;
import com.luckia.biller.core.model.UserActivityType;
import com.luckia.biller.core.model.common.Message;
import com.luckia.biller.core.services.AuditService;

public class CostCenterEntityService extends EntityService<CostCenter> {

	@Inject
	private AuditService auditService;

	@Override
	@Transactional
	@RegisterActivity(type = UserActivityType.COST_CENTER_MERGE)
	public Message<CostCenter> merge(CostCenter entity) {
		Message<CostCenter> validationResult = validate(entity);
		if (validationResult.hasErrors()) {
			return validationResult;
		}
		String message;
		EntityManager entityManager = entityManagerProvider.get();
		CostCenter current;
		if (entity.getId() == null) {
			current = new CostCenter();
			current.merge(entity);
			auditService.processCreated(current);
			entityManager.persist(current);
			message = i18nService.getMessage("costCenter.persist");
		} else {
			current = entityManager.find(CostCenter.class, entity.getId());
			current.merge(entity);
			auditService.processModified(current);
			entityManager.merge(current);
			message = i18nService.getMessage("costCenter.merge");
		}
		return new Message<CostCenter>(Message.CODE_SUCCESS, message, current);
	}

	@Override
	@Transactional
	@RegisterActivity(type = UserActivityType.COST_CENTER_REMOVE)
	public Message<CostCenter> remove(Serializable primaryKey) {
		EntityManager entityManager = entityManagerProvider.get();
		CostCenter current = entityManager.find(CostCenter.class, primaryKey);
		auditService.processDeleted(current);
		entityManager.merge(current);
		auditService.addUserActivity(UserActivityType.COST_CENTER_DELETE, primaryKey);
		return new Message<CostCenter>(Message.CODE_SUCCESS, i18nService.getMessage("costCenter.remove"), current);
	}

	@Override
	protected Class<CostCenter> getEntityClass() {
		return CostCenter.class;
	}
}
