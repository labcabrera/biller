/*******************************************************************************
 * Copyright (c) 2004, 2012 Kotasoft S.L.
 * All rights reserved. This program and the accompanying materials
 * may only be used prior written consent of Kotasoft S.L.
 ******************************************************************************/
package com.luckia.biller.core.services.entities;

import java.io.Serializable;

import javax.persistence.EntityManager;

import com.google.inject.persist.Transactional;
import com.luckia.biller.core.model.Company;
import com.luckia.biller.core.model.common.Message;

public class CompanyEntityService extends LegalEntityBaseService<Company> {

	@Override
	@Transactional
	public Message<Company> merge(Company entity) {
		Message<Company> validationResult = validate(entity);
		if (validationResult.hasErrors()) {
			return validationResult;
		}
		EntityManager entityManager = entityManagerProvider.get();
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
		return new Message<Company>(Message.CODE_SUCCESS, message, current);
	}

	@Override
	@Transactional
	public Message<Company> remove(Serializable primaryKey) {
		EntityManager entityManager = entityManagerProvider.get();
		Company current = entityManager.find(Company.class, primaryKey);
		auditService.processDeleted(current);
		entityManager.merge(current);
		return new Message<Company>(Message.CODE_SUCCESS, i18nService.getMessage("company.remove"), current);
	}

	@Override
	protected Class<Company> getEntityClass() {
		return Company.class;
	}
}
