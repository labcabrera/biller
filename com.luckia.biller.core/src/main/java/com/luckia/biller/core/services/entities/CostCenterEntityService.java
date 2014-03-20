package com.luckia.biller.core.services.entities;

import javax.persistence.EntityManager;

import com.luckia.biller.core.model.CostCenter;
import com.luckia.biller.core.model.common.Message;

public class CostCenterEntityService extends EntityService<CostCenter> {

	@Override
	public Message<CostCenter> merge(CostCenter entity) {
		String message;
		EntityManager entityManager = entityManagerProvider.get();
		CostCenter current;
		entityManager.getTransaction().begin();
		if (entity.getId() == null) {
			current = new CostCenter();
			current.merge(entity);
			entityManager.persist(current);
			message = "Centro de coste creado";
		} else {
			current = entityManager.find(CostCenter.class, entity.getId());
			current.merge(entity);
			entityManager.merge(current);
			message = "Centro de coste actualizado";
		}
		entityManager.getTransaction().commit();
		return new Message<CostCenter>(Message.CODE_SUCCESS, message, current);
	}

	@Override
	protected Class<CostCenter> getEntityClass() {
		return CostCenter.class;
	}
}
