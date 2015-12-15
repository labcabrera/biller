package com.luckia.biller.core.services;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Injector;
import com.luckia.biller.core.model.common.Message;
import com.luckia.biller.core.scheduler.tasks.SendAlertTask;
import com.luckia.biller.core.serialization.Serializer;

@Singleton
public class AlertService {

	private static final Logger LOG = LoggerFactory.getLogger(AlertService.class);

	private final Injector injector;
	private final Serializer serializer;
	private final ExecutorService executorService;

	@Inject
	public AlertService(Injector injector) {
		this.injector = injector;
		this.serializer = injector.getInstance(Serializer.class);
		this.executorService = Executors.newFixedThreadPool(5);
	}

	public void handleAlert(String message) {
		handleAlert(new Message<String>().withCode(Message.CODE_GENERIC_ERROR).addError(message));
	}

	public void handleAlert(Message<?> message) {
		LOG.error("Processing alert: {}", serializer.toJson(message));
		SendAlertTask task = new SendAlertTask(injector, message);
		executorService.submit(task);
	}
}
