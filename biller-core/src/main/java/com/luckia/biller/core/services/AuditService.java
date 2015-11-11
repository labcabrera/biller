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
import com.luckia.biller.core.serialization.Serializer;

public class AuditService {

	@Inject
	private SecurityService securityService;
	@Inject
	private Provider<EntityManager> entityManagerProvider;
	@Inject
	private Serializer serializer;

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

	public void addUserActivity(UserActivityType type, Object data) {
		addUserActivity(securityService.getCurrentUser(), type, data);
	}

	@Transactional
	public void addUserActivity(User user, UserActivityType type, Object data) {
		String dataStr = null;
		if (data != null && String.class.isAssignableFrom(data.getClass())) {
			dataStr = (String) data;
		} else if (data != null) {
			dataStr = serializer.toJson(data);
		}
		UserActivity entity = new UserActivity();
		entity.setId(UUID.randomUUID().toString());
		entity.setUser(user);
		entity.setData(dataStr);
		entity.setType(type);
		entity.setDate(new DateTime().toDate());
		EntityManager entityManager = entityManagerProvider.get();
		entityManager.persist(entity);
	}
}
