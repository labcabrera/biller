/*******************************************************************************
 * Copyright (c) 2004, 2012 Kotasoft S.L.
 * All rights reserved. This program and the accompanying materials
 * may only be used prior written consent of Kotasoft S.L.
 ******************************************************************************/
package com.luckia.biller.core.services.entities;

import java.io.Serializable;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Query;

import com.luckia.biller.core.model.CompanyGroup;
import com.luckia.biller.core.model.common.Message;
import com.luckia.biller.core.services.AuditService;

public class CompanyGroupEntityService extends LegalEntityBaseService<CompanyGroup> {

	@Inject
	private AuditService auditService;

	@Override
	public Message<CompanyGroup> merge(CompanyGroup entity) {
		Message<CompanyGroup> validationResult = validate(entity);
		if (validationResult.hasErrors()) {
			return validationResult;
		}
		EntityManager entityManager = entityManagerProvider.get();
		if (!entityManager.getTransaction().isActive()) {
			entityManager.getTransaction().begin();
		}
		Boolean isNew = entity.getId() == null;
		CompanyGroup current;
		if (isNew) {
			current = new CompanyGroup();
		} else {
			current = entityManager.find(CompanyGroup.class, entity.getId());
		}
		current.merge(entity);
		CompanyGroup merged = entityManager.merge(current);
		auditService.processModify(current);
		entityManager.getTransaction().commit();
		return new Message<CompanyGroup>(Message.CODE_SUCCESS, isNew ? "Grupo creado" : "Grupo actualizado", merged);
	}

	@Override
	public Message<CompanyGroup> remove(Serializable primaryKey) {
		EntityManager entityManager = entityManagerProvider.get();
		Query query = entityManager.createQuery("update LegalEntity e set e.parent = :parent where e.parent.id = :value");
		CompanyGroup entity = entityManager.find(CompanyGroup.class, primaryKey);
		entityManager.getTransaction().begin();
		query.setParameter("parent", null).setParameter("value", entity.getId()).executeUpdate();
		entityManager.remove(entity);
		entityManager.getTransaction().commit();
		return new Message<CompanyGroup>(Message.CODE_SUCCESS, "Grupo Eliminado", entity);
	}

	@Override
	protected Class<CompanyGroup> getEntityClass() {
		return CompanyGroup.class;
	}
}
