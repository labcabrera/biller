package com.luckia.biller.core.services.entities;

import java.io.Serializable;

import javax.inject.Inject;
import javax.persistence.EntityManager;

import com.luckia.biller.core.i18n.I18nService;
import com.luckia.biller.core.model.Store;
import com.luckia.biller.core.model.common.Message;
import com.luckia.biller.core.services.AuditService;

public class StoreEntityService extends LegalEntityBaseService<Store> {

	@Inject
	private AuditService auditService;
	@Inject
	private I18nService i18nService;

	@Override
	protected Class<Store> getEntityClass() {
		return Store.class;
	}

	@Override
	public Message<Store> merge(Store entity) {
		Message<Store> validationResult = validate(entity);
		if (validationResult.hasErrors()) {
			return validationResult;
		}
		EntityManager entityManager = entityManagerProvider.get();
		entityManager.getTransaction().begin();
		Store current;
		String message;
		if (entity.getId() == null) {
			current = new Store();
			current.merge(entity);
			auditService.processCreated(current);
			entityManager.persist(current);
			message = i18nService.getMessage("store.persist");
		} else {
			current = entityManager.find(Store.class, entity.getId());
			current.merge(entity);
			auditService.processModified(current);
			current = entityManager.merge(current);
			message = i18nService.getMessage("store.merge");
		}
		entityManager.getTransaction().commit();
		return new Message<Store>(Message.CODE_SUCCESS, message, current);
	}

	@Override
	public Message<Store> remove(Serializable primaryKey) {
		EntityManager entityManager = entityManagerProvider.get();
		Store current = entityManager.find(Store.class, primaryKey);
		entityManager.getTransaction().begin();
		auditService.processDeleted(current);
		entityManager.merge(current);
		entityManager.getTransaction().commit();
		return new Message<Store>(Message.CODE_SUCCESS, i18nService.getMessage("store.remove"), current);
	}
}
