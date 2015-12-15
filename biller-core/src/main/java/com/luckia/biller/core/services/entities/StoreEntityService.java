package com.luckia.biller.core.services.entities;

import java.io.Serializable;

import javax.persistence.EntityManager;

import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.persist.Transactional;
import com.luckia.biller.core.common.RegisterActivity;
import com.luckia.biller.core.model.BillingModel;
import com.luckia.biller.core.model.Store;
import com.luckia.biller.core.model.UserActivityType;
import com.luckia.biller.core.model.common.Message;

public class StoreEntityService extends LegalEntityBaseService<Store> {

	private static final Logger LOG = LoggerFactory.getLogger(StoreEntityService.class);

	@Override
	@Transactional
	@RegisterActivity(type = UserActivityType.STORE_MERGE)
	public Message<Store> merge(Store entity) {
		Validate.notNull(entity);
		EntityManager entityManager = entityManagerProvider.get();
		Store current;
		String message;
		if (entity.getBillingModel() != null) {
			BillingModel currentModel = entityManager.find(BillingModel.class, entity.getBillingModel().getId());
			entity.setBillingModel(currentModel);
		}
		if (entity.getId() == null) {
			LOG.info("Creating new store");
			current = new Store();
			current.merge(entity);
			auditService.processCreated(current);
			entityManager.persist(current);
			message = i18nService.getMessage("store.persist");
		} else {
			LOG.info("Updating current store");
			current = entityManager.find(Store.class, entity.getId());
			current.merge(entity);
			auditService.processModified(current);
			current = entityManager.merge(current);
			message = i18nService.getMessage("store.merge");
		}
		entityManager.flush();
		entityManager.refresh(current);
		return new Message<>(Message.CODE_SUCCESS, message, current);

	}

	@Override
	@Transactional
	@RegisterActivity(type = UserActivityType.STORE_REMOVE)
	public Message<Store> remove(Serializable primaryKey) {
		EntityManager entityManager = entityManagerProvider.get();
		Store current = entityManager.find(Store.class, primaryKey);
		auditService.processDeleted(current);
		entityManager.merge(current);
		return new Message<>(Message.CODE_SUCCESS, i18nService.getMessage("store.remove"), current);
	}

	@Override
	protected Class<Store> getEntityClass() {
		return Store.class;
	}

}
