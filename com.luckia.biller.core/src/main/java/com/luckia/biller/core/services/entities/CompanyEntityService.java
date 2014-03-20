/*******************************************************************************
 * Copyright (c) 2004, 2012 Kotasoft S.L.
 * All rights reserved. This program and the accompanying materials
 * may only be used prior written consent of Kotasoft S.L.
 ******************************************************************************/
package com.luckia.biller.core.services.entities;

import javax.persistence.EntityManager;

import com.luckia.biller.core.model.Address;
import com.luckia.biller.core.model.Company;
import com.luckia.biller.core.model.IdCard;
import com.luckia.biller.core.model.common.Message;

public class CompanyEntityService extends LegalEntityBaseService<Company> {

	@Override
	public Message<Company> merge(Company entity) {
		Message<Company> validationResult = validate(entity);
		if (validationResult.hasErrors()) {
			return validationResult;
		}
		EntityManager entityManager = entityManagerProvider.get();
		if (!entityManager.getTransaction().isActive()) {
			entityManager.getTransaction().begin();
		}
		Boolean isNew = entity.getId() == null;
		Company current;
		if (isNew) {
			current = new Company();
			current.setIdCard(new IdCard());
			current.setAddress(new Address());
		} else {
			current = entityManager.find(Company.class, entity.getId());
		}
		current.merge(entity);
		Company merged = entityManager.merge(current);
		entityManager.getTransaction().commit();
		return new Message<Company>(Message.CODE_SUCCESS, isNew ? "Empresa creada" : "Empresa actualizada", merged);
	}

	@Override
	protected Class<Company> getEntityClass() {
		return Company.class;
	}
}
