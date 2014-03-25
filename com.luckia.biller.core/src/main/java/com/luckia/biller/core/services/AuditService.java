package com.luckia.biller.core.services;

import java.util.Calendar;
import java.util.Date;

import javax.inject.Inject;

import com.luckia.biller.core.model.Auditable;

// TODO ----------------------------- CREAR UNA ENTIDAD Y HACERLA EMBEDDABLE CON TODAS LAS FECHAS Y EL USUARIO ---------------
public class AuditService {

	@Inject
	private SecurityService securityService;

	public void processCreated(Auditable auditable) {
		Date now = Calendar.getInstance().getTime();
		auditable.setCreated(now);
		auditable.setModifiedBy(securityService.getCurrentUser());
	}

	public void processDelete(Auditable auditable) {
		Date now = Calendar.getInstance().getTime();
		auditable.setDeleted(now);
		auditable.setModified(now);
		auditable.setModifiedBy(securityService.getCurrentUser());
	}

	public void processModify(Auditable auditable) {
		Date now = Calendar.getInstance().getTime();
		auditable.setModified(now);
		auditable.setModifiedBy(securityService.getCurrentUser());
	}
}
