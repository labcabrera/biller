package com.luckia.biller.core.services;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.persistence.EntityManager;

import com.luckia.biller.core.model.AppSettings;

/**
 * Servicio que proporciona acceso a los párametros de configuración de la aplicación. La configuración está dividida en categorias:
 * <ul>
 * <li>MAIL: propiedades para el envío de correos.</li>
 * <li>BILLING: propiedades específicas de la facturación (porcentaje de IVA por ejemplo).</li>
 * <li>SYSTEM: propiedades del sistema tales como la ruta del repositorio de ficheros o expresiones CRON de las tareas programadas.</li>
 * </ul>
 */
public class SettingsService {

	public static final String MAIL = "MAIL";
	public static final String BILLING = "BILLING";
	public static final String SYSTEM = "SYSTEM";

	@Inject
	private Provider<EntityManager> entityManagerProvider;

	public AppSettings getMailSettings() {
		return entityManagerProvider.get().find(AppSettings.class, MAIL);
	}

	public AppSettings getBillingSettings() {
		return entityManagerProvider.get().find(AppSettings.class, BILLING);
	}

	public AppSettings getSystemSettings() {
		return entityManagerProvider.get().find(AppSettings.class, SYSTEM);
	}
}
