package com.luckia.biller.core.services;

import java.util.Calendar;
import java.util.Date;

import javax.inject.Inject;

import com.luckia.biller.core.model.AuditData;
import com.luckia.biller.core.model.Auditable;

public class AuditService {

	@Inject
	private SecurityService securityService;

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
}
