package com.luckia.biller.core.services.entities;

import java.io.Serializable;

import javax.persistence.EntityManager;

import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.luckia.biller.core.ClearCache;
import com.luckia.biller.core.model.Store;
import com.luckia.biller.core.model.common.Message;

public class StoreEntityService extends LegalEntityBaseService<Store> {

	private static final Logger LOG = LoggerFactory.getLogger(StoreEntityService.class);

	@Override
	@ClearCache
	public Message<Store> merge(Store entity) {
		Validate.notNull(entity);
		LOG.info("Merge store {}", entity.getName());
		Message<Store> validationResult = validate(entity);
		if (validationResult.hasErrors()) {
			return validationResult;
		}
		EntityManager entityManager = entityManagerProvider.get();
		if (entityManager.getTransaction().isActive()) {
			try {
				LOG.warn("Transaccion en curso no esperada: forzando commit");
				entityManager.getTransaction().commit();
			} catch (Exception ignore) {
				LOG.warn("Error al realizar el commit de la transaccion no esperada");
			}
		}
		entityManager.getTransaction().begin();
		Store current;
		String message;
		if (entity.getId() == null) {
			LOG.info("Creating new store");
			current = new Store();
			current.merge(entity);
			auditService.processCreated(current);
			entityManager.persist(current);
			message = i18nService.getMessage("store.persist");
		} else {
			LOG.info("Creating new store");
			current = entityManager.find(Store.class, entity.getId());
			current.merge(entity);
			auditService.processModified(current);
			current = entityManager.merge(current);
			message = i18nService.getMessage("store.merge");
		}
		entityManager.getTransaction().commit();
		return new Message<>(Message.CODE_SUCCESS, message, current);
	}

	@Override
	@ClearCache
	public Message<Store> remove(Serializable primaryKey) {
		EntityManager entityManager = entityManagerProvider.get();
		Store current = entityManager.find(Store.class, primaryKey);
		entityManager.getTransaction().begin();
		auditService.processDeleted(current);
		entityManager.merge(current);
		entityManager.getTransaction().commit();
		return new Message<>(Message.CODE_SUCCESS, i18nService.getMessage("store.remove"), current);
	}

	@Override
	protected Class<Store> getEntityClass() {
		return Store.class;
	}

}
