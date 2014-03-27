/*******************************************************************************
 * Copyright (c) 2004, 2012 Kotasoft S.L.
 * All rights reserved. This program and the accompanying materials
 * may only be used prior written consent of Kotasoft S.L.
 ******************************************************************************/
package com.luckia.biller.core.services.entities;

import java.io.Serializable;

import javax.inject.Inject;
import javax.persistence.EntityManager;

import com.luckia.biller.core.i18n.I18nService;
import com.luckia.biller.core.model.Company;
import com.luckia.biller.core.model.common.Message;
import com.luckia.biller.core.services.AuditService;

public class CompanyEntityService extends LegalEntityBaseService<Company> {

	@Inject
	private I18nService i18nService;
	@Inject
	private AuditService auditService;

	@Override
	public Message<Company> merge(Company entity) {
		Message<Company> validationResult = validate(entity);
		if (validationResult.hasErrors()) {
			return validationResult;
		}
		EntityManager entityManager = entityManagerProvider.get();
		entityManager.getTransaction().begin();
		Company current;
		String message;
		if (entity.getId() == null) {
			current = new Company();
			current.merge(entity);
			auditService.processCreated(current);
			entityManager.persist(current);
			message = i18nService.getMessage("company.persist");
		} else {
			current = entityManager.find(Company.class, entity.getId());
			current.merge(entity);
			auditService.processModified(current);
			entityManager.merge(current);
			message = i18nService.getMessage("company.merge");
		}
		entityManager.getTransaction().commit();
		return new Message<Company>(Message.CODE_SUCCESS, message, current);
	}

	@Override
	public Message<Company> remove(Serializable primaryKey) {
		EntityManager entityManager = entityManagerProvider.get();
		Company current = entityManager.find(Company.class, primaryKey);
		entityManager.getTransaction().begin();
		auditService.processDeleted(current);
		entityManager.merge(current);
		entityManager.getTransaction().commit();
		return new Message<Company>(Message.CODE_SUCCESS, i18nService.getMessage("company.remove"), current);
	}

	@Override
	protected Class<Company> getEntityClass() {
		return Company.class;
	}
}
