package com.luckia.biller.core.services.entities;

import java.io.Serializable;

import javax.persistence.EntityManager;

import com.google.inject.persist.Transactional;
import com.luckia.biller.core.common.RegisterActivity;
import com.luckia.biller.core.model.Owner;
import com.luckia.biller.core.model.UserActivityType;
import com.luckia.biller.core.model.common.Message;

/**
 * {@link EntityService} asociado a la entidad {@link Owner}
 */
public class OwnerEntityService extends LegalEntityBaseService<Owner> {

	@Override
	@Transactional
	@RegisterActivity(type = UserActivityType.OWNER_MERGE)
	public Message<Owner> merge(Owner entity) {
		Message<Owner> validationResult = validate(entity);
		if (validationResult.hasErrors()) {
			return validationResult;
		}
		Owner current;
		String message;
		EntityManager entityManager = entityManagerProvider.get();
		if (entity.getId() == null) {
			current = new Owner();
			current.merge(entity);
			auditService.processCreated(current);
			entityManager.persist(current);
			message = i18nService.getMessage("owner.persist");
		} else {
			current = entityManager.find(Owner.class, entity.getId());
			current.merge(entity);
			auditService.processModified(current);
			entityManager.merge(current);
			message = i18nService.getMessage("owner.merge");
		}
		return new Message<Owner>(Message.CODE_SUCCESS, message, current);
	}

	@Override
	@Transactional
	@RegisterActivity(type = UserActivityType.OWNER_REMOVE)
	public Message<Owner> remove(Serializable primaryKey) {
		EntityManager entityManager = entityManagerProvider.get();
		Owner current = entityManager.find(Owner.class, primaryKey);
		auditService.processDeleted(current);
		entityManager.merge(current);
		return new Message<Owner>(Message.CODE_SUCCESS, i18nService.getMessage("owner.remove"), current);
	}

	@Override
	protected Class<Owner> getEntityClass() {
		return Owner.class;
	}
}
