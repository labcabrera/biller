package com.luckia.biller.core.services.entities;

import java.io.Serializable;

import javax.persistence.EntityManager;

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
			if (entity.getModel() == null || entity.getModel().getId() == null) {
				return new Message<>(Message.CODE_GENERIC_ERROR, i18nService.getMessage("billingModel.rappel.error.missingModel"));
			}
			EntityManager entityManager = entityManagerProvider.get();
			entityManager.getTransaction().begin();
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
			entityManager.getTransaction().commit();
			// TODO no esta funcionando de modo que no refresca los cambios
			entityManager.detach(model);
			entityManager.clear();
			model = entityManager.find(BillingModel.class, entity.getModel().getId());
			return new Message<>(Message.CODE_SUCCESS, message, model);
		} catch (Exception ex) {
			return new Message<>(Message.CODE_GENERIC_ERROR, i18nService.getMessage("billingModel.rappel.merge"));
		}
	}

	public Message<BillingModel> removeRappelDetail(Long primaryKey) {
		throw new RuntimeException("NOT (YET) IMPLEMENTED!");
	}

	@Override
	protected Class<BillingModel> getEntityClass() {
		return BillingModel.class;
	}
}
