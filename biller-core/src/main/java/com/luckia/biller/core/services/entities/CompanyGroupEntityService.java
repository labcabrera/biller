package com.luckia.biller.core.services.entities;

import java.io.Serializable;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import com.google.inject.persist.Transactional;
import com.luckia.biller.core.common.RegisterActivity;
import com.luckia.biller.core.model.CompanyGroup;
import com.luckia.biller.core.model.UserActivityType;
import com.luckia.biller.core.model.common.Message;

public class CompanyGroupEntityService extends LegalEntityBaseService<CompanyGroup> {

	@Override
	@Transactional
	@RegisterActivity(type = UserActivityType.COMPANY_GROUP_MERGE)
	public Message<CompanyGroup> merge(CompanyGroup entity) {
		Message<CompanyGroup> validationResult = validate(entity);
		if (validationResult.hasErrors()) {
			return validationResult;
		}
		EntityManager entityManager = entityManagerProvider.get();
		CompanyGroup current;
		String message;
		if (entity.getId() == null) {
			current = new CompanyGroup();
			current.merge(entity);
			auditService.processCreated(current);
			entityManager.persist(current);
			message = i18nService.getMessage("companyGroup.persist");
		}
		else {
			current = entityManager.find(CompanyGroup.class, entity.getId());
			current.merge(entity);
			auditService.processModified(current);
			entityManager.persist(current);
			message = i18nService.getMessage("companyGroup.merge");
		}
		entityManager.flush();
		entityManager.refresh(current);
		return new Message<CompanyGroup>(Message.CODE_SUCCESS, message, current);
	}

	@Override
	@Transactional
	@RegisterActivity(type = UserActivityType.COMPANY_GROUP_REMOVE)
	public Message<CompanyGroup> remove(Serializable primaryKey) {
		EntityManager entityManager = entityManagerProvider.get();
		Query query = entityManager.createQuery(
				"update LegalEntity e set e.parent = :parent where e.parent.id = :value");
		CompanyGroup entity = entityManager.find(CompanyGroup.class, primaryKey);
		query.setParameter("parent", null).setParameter("value", entity.getId())
				.executeUpdate();
		auditService.processDeleted(entity);
		return new Message<CompanyGroup>(Message.CODE_SUCCESS,
				i18nService.getMessage("companyGroup.remove"), entity);
	}

	@Override
	protected Class<CompanyGroup> getEntityClass() {
		return CompanyGroup.class;
	}
}
