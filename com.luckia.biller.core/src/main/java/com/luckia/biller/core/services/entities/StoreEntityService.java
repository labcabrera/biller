package com.luckia.biller.core.services.entities;

import javax.persistence.EntityManager;

import com.luckia.biller.core.model.Store;
import com.luckia.biller.core.model.common.Message;

public class StoreEntityService extends LegalEntityBaseService<Store> {

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
			entityManager.persist(current);
			message = "Establecimiento creado";
		} else {
			current = entityManager.find(Store.class, entity.getId());
			current.merge(entity);
			current = entityManager.merge(current);
			message = "Establecimiento actualizado";
		}
		entityManager.getTransaction().commit();
		return new Message<Store>(Message.CODE_SUCCESS, message, current);
	}
}
