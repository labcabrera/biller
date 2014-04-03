package com.luckia.biller.deploy.fedders;

import java.io.InputStream;

import javax.inject.Inject;
import javax.persistence.EntityManager;

import com.luckia.biller.core.jpa.EntityManagerProvider;
import com.luckia.biller.core.model.AppSettings;
import com.luckia.biller.core.services.SettingsService;

public class AppSettingsFeeder implements Feeder<AppSettings> {

	@Inject
	private EntityManagerProvider entityManagerProvider;

	@Override
	public void loadEntities(InputStream source) {
		EntityManager entityManager = entityManagerProvider.get();

		AppSettings mailSettings = new AppSettings();
		mailSettings.setId(SettingsService.MAIL);
		mailSettings.setValue("emailUser", "lab.cabrera");
		mailSettings.setValue("emailPassword", "#####");
		mailSettings.setValue("hostName", "smtp.gmail.com");
		mailSettings.setValue("fromEmail", "lab.cabrera@gmail.com");
		mailSettings.setValue("fromName", "lab.cabrera");
		mailSettings.setValue("sslConnection", "true");

		AppSettings billingSettings = new AppSettings();
		billingSettings.setId(SettingsService.BILLING);
		billingSettings.setValue("vat", "21.00");

		AppSettings systemSettings = new AppSettings();
		systemSettings.setId(SettingsService.SYSTEM);
		systemSettings.setValue("repositoryPath", "/home/lab/.biller/files/");
		// Dia 2 de cada mes a las 2:00 AM
		systemSettings.setValue("job.biller.cron", "0 0 2 2 1/1 ? *");
		// Dia 2 de cada mes a las 3:00 AM
		systemSettings.setValue("job.rappel.cron", "0 0 3 2 1/1 ? *");
		// Cada hora
		// systemSettings.setValue("job.system.check.cron", "0 0 0/1 1/1 * ? *");
		// Cada minuto
		systemSettings.setValue("job.system.check.cron", "0 0/1 * 1/1 * ? *");

		entityManager.persist(mailSettings);
		entityManager.persist(billingSettings);
		entityManager.persist(systemSettings);
	}
}
