package com.luckia.biller.core.services.entities;

import javax.persistence.EntityManager;

import com.luckia.biller.core.model.Owner;
import com.luckia.biller.core.model.common.Message;

/**
 * {@link EntityService} asociado a la entidad {@link Owner}
 */
public class OwnerEntityService extends LegalEntityBaseService<Owner> {

	@Override
	public Message<Owner> merge(Owner entity) {
		Message<Owner> validationResult = validate(entity);
		if (validationResult.hasErrors()) {
			return validationResult;
		}
		Owner current;
		String message;
		EntityManager entityManager = entityManagerProvider.get();
		entityManager.getTransaction().begin();
		if (entity.getId() == null) {
			current = new Owner();
			current.merge(entity);
			entityManager.persist(current);
			message = "Titular creado";
		} else {
			current = entityManager.find(Owner.class, entity.getId());
			current.merge(entity);
			entityManager.merge(current);
			message = "Titular actualizado";
		}
		entityManager.getTransaction().commit();
		return new Message<Owner>(Message.CODE_SUCCESS, message, current);
	}

	@Override
	protected Class<Owner> getEntityClass() {
		return Owner.class;
	}
}
