package com.luckia.biller.core.services.entities;

import javax.persistence.EntityManager;

import com.luckia.biller.core.model.BillingModel;
import com.luckia.biller.core.model.common.Message;

public class BillingModelEntityService extends EntityService<BillingModel> {

	@Override
	public Message<BillingModel> merge(BillingModel entity) {
		EntityManager entityManager = entityManagerProvider.get();
		entityManager.getTransaction().begin();
		Boolean isNewEntity = entity.getId() == null;
		BillingModel current;
		if (isNewEntity) {
			current = new BillingModel();
			current.merge(entity);
			entityManager.persist(current);
		} else {
			current = entityManager.find(BillingModel.class, entity.getId());
			current.merge(entity);
			entityManager.persist(current);
		}
		entityManager.getTransaction().commit();
		return new Message<BillingModel>(Message.CODE_SUCCESS, isNewEntity ? "Modelo creado" : "Modelo actualizado", current);
	}

	@Override
	protected Class<BillingModel> getEntityClass() {
		return BillingModel.class;
	}

}
