package com.luckia.biller.core.services;

import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.persistence.EntityManager;

import org.joda.time.DateTime;

import com.google.inject.persist.Transactional;
import com.luckia.biller.core.model.AuditData;
import com.luckia.biller.core.model.Auditable;
import com.luckia.biller.core.model.User;
import com.luckia.biller.core.model.UserActivity;
import com.luckia.biller.core.model.UserActivityType;

public class AuditService {

	@Inject
	private SecurityService securityService;
	@Inject
	private Provider<EntityManager> entityManagerProvider;

	public void processCreated(Auditable auditable) {
		Date now = Calendar.getInstance().getTime();
		if (auditable.getAuditData() == null) {
			auditable.setAuditData(new AuditData());
		}
		auditable.getAuditData().setCreated(now);
		auditable.getAuditData().setModifiedBy(securityService.getCurrentUser());
	}

	public void processDeleted(Auditable auditable) {
		Date now = Calendar.getInstance().getTime();
		auditable.getAuditData().setDeleted(now);
		auditable.getAuditData().setModified(now);
		auditable.getAuditData().setModifiedBy(securityService.getCurrentUser());
	}

	public void processModified(Auditable auditable) {
		Date now = Calendar.getInstance().getTime();
		if (auditable.getAuditData() == null) {
			auditable.setAuditData(new AuditData());
		}
		if (auditable.getAuditData().getCreated() == null) {
			auditable.getAuditData().setCreated(now);
		}
		auditable.getAuditData().setModified(now);
		auditable.getAuditData().setModifiedBy(securityService.getCurrentUser());
	}

	@Transactional
	public void addUserActivity(User user, UserActivityType type, String data) {
		UserActivity entity = new UserActivity();
		entity.setId(UUID.randomUUID().toString());
		entity.setUser(user);
		entity.setData(data);
		entity.setType(type);
		entity.setDate(new DateTime().toDate());
		EntityManager entityManager = entityManagerProvider.get();
		entityManager.persist(entity);
	}
}
