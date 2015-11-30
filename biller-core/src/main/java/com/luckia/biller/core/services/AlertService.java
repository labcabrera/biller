package com.luckia.biller.core.services;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.luckia.biller.core.model.common.Message;
import com.luckia.biller.core.serialization.Serializer;

//TODO de momento solo se genera una traza de log. Habria que mirar en que casos puede ser util enviar un mensaje de error
public class AlertService {

	private static final Logger LOG = LoggerFactory.getLogger(AlertService.class);

	@Inject
	private Serializer serializer;

	public void hangleAlert(String message) {
		LOG.error("System alert: {}", message);
	}

	public void handleAlert(Message<?> message) {
		LOG.error("System alert: {}", serializer.toJson(message));
	}
}
