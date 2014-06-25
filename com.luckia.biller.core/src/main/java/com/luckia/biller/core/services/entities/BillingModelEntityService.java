package com.luckia.biller.core.services.entities;

import java.io.Serializable;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.validation.ConstraintViolation;

import org.apache.commons.lang3.Validate;

import com.luckia.biller.core.model.BillingModel;
import com.luckia.biller.core.model.Rappel;
import com.luckia.biller.core.model.common.Message;

public class BillingModelEntityService extends EntityService<BillingModel> {

	@Override
	public Message<BillingModel> merge(BillingModel entity) {
		EntityManager entityManager = entityManagerProvider.get();
		entityManager.getTransaction().begin();
		BillingModel current;
		String message;
		if (entity.getId() == null) {
			current = new BillingModel();
			auditService.processCreated(current);
			current.merge(entity);
			entityManager.persist(current);
			message = i18nService.getMessage("billingModel.persit");
		} else {
			current = entityManager.find(BillingModel.class, entity.getId());
			current.merge(entity);
			auditService.processModified(current);
			entityManager.merge(current);
			message = i18nService.getMessage("billingModel.merge");
		}
		entityManager.getTransaction().commit();
		return new Message<BillingModel>(Message.CODE_SUCCESS, message, current);
	}

	@Override
	public Message<BillingModel> remove(Serializable primaryKey) {
		EntityManager entityManager = entityManagerProvider.get();
		BillingModel current = entityManager.find(BillingModel.class, primaryKey);
		entityManager.getTransaction().begin();
		auditService.processDeleted(current);
		entityManager.merge(current);
		entityManager.getTransaction().commit();
		return new Message<BillingModel>(Message.CODE_SUCCESS, i18nService.getMessage("billingModel.remove"), current);
	}

	public Message<BillingModel> mergeRappelDetail(Rappel entity) {
		try {
			// TODO deberiamos tener un constructor de mensajes a partir de errores de validacion en lugar de tomar solo el primer mensaje
			Set<ConstraintViolation<Rappel>> violations = validator.validate(entity);
			if (!violations.isEmpty()) {
				String messageKey = violations.iterator().next().getMessage();
				return new Message<>(Message.CODE_GENERIC_ERROR, i18nService.getMessage(messageKey));
			}
			EntityManager entityManager = entityManagerProvider.get();
			BillingModel model = entityManager.find(BillingModel.class, entity.getModel().getId());
			Rappel current;
			String message;
			entityManager.getTransaction().begin();
			if (entity.getId() == null) {
				current = new Rappel();
				current.merge(entity);
				current.setModel(model);
				entityManager.persist(current);
				message = i18nService.getMessage("billingModel.rappel.persist");
			} else {
				current = entityManager.find(Rappel.class, entity.getId());
				current.merge(entity);
				entityManager.merge(current);
				message = i18nService.getMessage("billingModel.rappel.merge");
			}
			entityManager.getTransaction().commit();
			entityManager.refresh(model);
			entityManager.getTransaction().begin();
			auditService.processModified(model);
			entityManager.getTransaction().commit();
			return new Message<>(Message.CODE_SUCCESS, message, model);
		} catch (Exception ex) {
			return new Message<>(Message.CODE_GENERIC_ERROR, i18nService.getMessage("billingModel.rappel.merge"));
		}
	}

	public Message<BillingModel> removeRappelDetail(Long primaryKey) {
		try {
			Validate.notNull(primaryKey);
			EntityManager entityManager = entityManagerProvider.get();
			Rappel rappel = entityManager.find(Rappel.class, primaryKey);
			Validate.notNull(rappel);
			BillingModel model = rappel.getModel();
			entityManager.getTransaction().begin();
			entityManager.remove(rappel);
			entityManager.getTransaction().commit();
			entityManager.refresh(model);
			entityManager.getTransaction().begin();
			auditService.processModified(model);
			entityManager.merge(model);
			entityManager.getTransaction().commit();
			String message = i18nService.getMessage("billingModel.rappel.remove");
			return new Message<>(Message.CODE_SUCCESS, message, model);
		} catch (Exception ex) {
			return new Message<>(Message.CODE_GENERIC_ERROR, i18nService.getMessage("billingModel.rappel.error.remove"));
		}
	}

	@Override
	protected Class<BillingModel> getEntityClass() {
		return BillingModel.class;
	}
}
