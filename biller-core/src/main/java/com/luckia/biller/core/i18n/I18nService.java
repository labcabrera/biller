/*******************************************************************************
 * Copyright (c) 2004, 2012 Kotasoft S.L.
 * All rights reserved. This program and the accompanying materials
 * may only be used prior written consent of Kotasoft S.L.
 ******************************************************************************/
package com.luckia.biller.core.i18n;

import java.util.Locale;
import java.util.ResourceBundle;

import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Servicio encargado de obtener los mensajes de la aplicacion. Los mensajes se recuperan desde el bundle
 * <code>luckia-messages.properties</code>.
 */
@Singleton
public class I18nService {

	private static final Logger LOG = LoggerFactory.getLogger(I18nService.class);

	private final ResourceBundle messages;

	/**
	 * Constructor que inicializa el <code>ResourceBundle</code>.
	 */
	public I18nService() {
		Locale locale = new Locale("es", "ES");
		messages = ResourceBundle.getBundle("luckia-messages", locale);
	}

	/**
	 * Obtiene el literal asociado a la clave a partir.
	 * 
	 * @param key
	 * @return
	 */
	public String getMessage(String key) {
		if (messages.containsKey(key)) {
			return messages.getString(key);
		} else {
			LOG.warn("Missing message {}", key);
			return String.format("*%s*", key);
		}
	}
}
