package com.luckia.biller.core.services;

import java.util.Calendar;
import java.util.Date;

import javax.inject.Inject;

import com.luckia.biller.core.model.Auditable;

// TODO ----------------------------- CREAR UNA ENTIDAD Y HACERLA EMBEDDABLE CON TODAS LAS FECHAS Y EL USUARIO ---------------
public class AuditService {

	@Inject
	private SecurityService securityService;

	public void initEntity(Auditable auditable) {
		Date now = Calendar.getInstance().getTime();
		auditable.setCreated(now);
		auditable.setModifiedBy(securityService.getCurrentUser());
	}

	public void delete(Auditable auditable) {
		// AuditInfo data = auditable.getAuditInfo();
		// Date now = Calendar.getInstance().getTime();
		// data.setDeleted(now);
		// data.setModified(now);
		// data.setModifiedBy(securityService.getCurrentUser());
		// entityManagerProvider.get().merge(auditable);
	}

	public void modify(Auditable auditable) {
		// AuditInfo data = auditable.getAuditInfo();
		// Date now = Calendar.getInstance().getTime();
		// data.setModified(now);
		// data.setModifiedBy(securityService.getCurrentUser());
		// entityManagerProvider.get().merge(auditable);
	}
}
