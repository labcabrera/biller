package com.luckia.biller.core.services.entities;

import java.io.Serializable;

import javax.persistence.EntityManager;

import com.luckia.biller.core.model.CostCenter;
import com.luckia.biller.core.model.common.Message;

public class CostCenterEntityService extends EntityService<CostCenter> {

	@Override
	public Message<CostCenter> merge(CostCenter entity) {
		Message<CostCenter> validationResult = validate(entity);
		if (validationResult.hasErrors()) {
			return validationResult;
		}
		String message;
		EntityManager entityManager = entityManagerProvider.get();
		CostCenter current;
		entityManager.getTransaction().begin();
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
		entityManager.getTransaction().commit();
		return new Message<CostCenter>(Message.CODE_SUCCESS, message, current);
	}

	@Override
	public Message<CostCenter> remove(Serializable primaryKey) {
		EntityManager entityManager = entityManagerProvider.get();
		CostCenter current = entityManager.find(CostCenter.class, primaryKey);
		entityManager.getTransaction().begin();
		auditService.processDeleted(current);
		entityManager.merge(current);
		entityManager.getTransaction().commit();
		return new Message<CostCenter>(Message.CODE_SUCCESS, i18nService.getMessage("costCenter.remove"), current);
	}

	@Override
	protected Class<CostCenter> getEntityClass() {
		return CostCenter.class;
	}
}
