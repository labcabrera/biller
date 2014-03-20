package com.luckia.biller.core.services.entities;

import javax.persistence.EntityManager;

import com.luckia.biller.core.model.Address;
import com.luckia.biller.core.model.IdCard;
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
		if (!entityManager.getTransaction().isActive()) {
			entityManager.getTransaction().begin();
		}
		Boolean isNew = entity.getId() == null;
		Store current;
		if (isNew) {
			current = new Store();
			current.setIdCard(new IdCard());
			current.setAddress(new Address());
		} else {
			current = entityManager.find(Store.class, entity.getId());
		}
		current.merge(entity);
		current.setType(entity.getType());
		current.setOwner(entity.getOwner());
		current.setBillingModel(entity.getBillingModel());
		Store merged = entityManager.merge(current);
		entityManager.getTransaction().commit();
		return new Message<Store>(Message.CODE_SUCCESS, isNew ? "Establecimiento creado" : "Establecimiento actualizado", merged);
	}
}
