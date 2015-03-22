package com.luckia.biller.core.services.entities;

import java.io.Serializable;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.validation.ConstraintViolation;

import org.apache.commons.lang3.Validate;

import com.google.inject.persist.Transactional;
import com.luckia.biller.core.model.BillingModel;
import com.luckia.biller.core.model.Rappel;
import com.luckia.biller.core.model.common.Message;

public class BillingModelEntityService extends EntityService<BillingModel> {

	@Override
	@Transactional
	public Message<BillingModel> merge(BillingModel entity) {
		EntityManager entityManager = entityManagerProvider.get();
		BillingModel current;
		String message;
		if (entity.getId() == null) {
			current = new BillingModel();
			auditService.processCreated(current);
			current.merge(entity);
			entityManager.persist(current);
			message = i18nService.getMessage("billingModel.persist");
		} else {
			current = entityManager.find(BillingModel.class, entity.getId());
			current.merge(entity);
			auditService.processModified(current);
			entityManager.merge(current);
			message = i18nService.getMessage("billingModel.merge");
		}
		return new Message<BillingModel>(Message.CODE_SUCCESS, message, current);
	}

	@Override
	@Transactional
	public Message<BillingModel> remove(Serializable primaryKey) {
		EntityManager entityManager = entityManagerProvider.get();
		BillingModel current = entityManager.find(BillingModel.class, primaryKey);
		auditService.processDeleted(current);
		entityManager.merge(current);
		return new Message<BillingModel>(Message.CODE_SUCCESS, i18nService.getMessage("billingModel.remove"), current);
	}

	@Transactional
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
			auditService.processModified(model);
			return new Message<>(Message.CODE_SUCCESS, message, model);
		} catch (Exception ex) {
			return new Message<>(Message.CODE_GENERIC_ERROR, i18nService.getMessage("billingModel.rappel.merge"));
		}
	}

	@Transactional
	public Message<BillingModel> removeRappelDetail(Long primaryKey) {
		try {
			Validate.notNull(primaryKey);
			EntityManager entityManager = entityManagerProvider.get();
			Rappel rappel = entityManager.find(Rappel.class, primaryKey);
			Validate.notNull(rappel);
			BillingModel model = rappel.getModel();
			entityManager.remove(rappel);
			entityManager.flush();
			entityManager.refresh(model);
			auditService.processModified(model);
			entityManager.merge(model);
			entityManager.flush();
			String message = i18nService.getMessage("billingModel.rappel.remove");
			return new Message<>(Message.CODE_SUCCESS, message, model);
		} catch (Exception ex) {
			return new Message<>(Message.CODE_GENERIC_ERROR, i18nService.getMessage("billingModel.rappel.error.remove"));
		}
	}

	@Override
	protected void buildOrderCriteria(CriteriaQuery<BillingModel> criteria, CriteriaBuilder builder, Root<BillingModel> root) {
		criteria.orderBy(builder.asc(root.<String> get("name")));
	}

	@Override
	protected Class<BillingModel> getEntityClass() {
		return BillingModel.class;
	}
}
