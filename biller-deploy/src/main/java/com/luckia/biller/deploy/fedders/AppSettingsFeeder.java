package com.luckia.biller.deploy.fedders;

import java.io.InputStream;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.persistence.EntityManager;

import com.luckia.biller.core.model.AppSettings;
import com.luckia.biller.core.services.SettingsService;

public class AppSettingsFeeder implements Feeder<AppSettings> {

	@Inject
	private Provider<EntityManager> entityManagerProvider;

	@Override
	public void loadEntities(InputStream source) {
		EntityManager entityManager = entityManagerProvider.get();
		AppSettings mailSettings = new AppSettings();
		mailSettings.setId(SettingsService.MAIL);
		mailSettings.setValue("emailUser", "notificaciones@luckia.com");
		mailSettings.setValue("emailPassword", "N7r2or59");
		mailSettings.setValue("hostName", "smtp.office365.com");
		mailSettings.setValue("fromEmail", "notificaciones@luckia.com");
		mailSettings.setValue("fromName", "Notificaciones Luckia");
		mailSettings.setValue("sslConnection", "false");
		mailSettings.setValue("tlsConnection", "true");
		mailSettings.setValue("port", "587");

		AppSettings systemSettings = new AppSettings();
		systemSettings.setId(SettingsService.SYSTEM);
		systemSettings.setValue("repositoryPath", "/home/lab/.biller/files/");
		systemSettings.setValue("job.biller.cron", "0 0 2 2 1/1 ? *");
		systemSettings.setValue("job.rappel.cron", "0 0 3 2 1/1 ? *");
		systemSettings.setValue("job.system.check.cron", "0 0/1 * 1/1 * ? *");

		entityManager.persist(mailSettings);
		entityManager.persist(systemSettings);
	}
}
