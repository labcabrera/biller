package com.luckia.biller.core.scheduler.tasks;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import com.google.inject.Injector;
import com.luckia.biller.core.model.AlertReceiver;
import com.luckia.biller.core.model.common.Message;
import com.luckia.biller.core.services.mail.MailMessageConverter;
import com.luckia.biller.core.services.mail.MailService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
public class SendAlertTask implements Runnable {

	private final Injector injector;
	private final Message<?> message;

	@Override
	public void run() {
		EntityManager entityManager = injector.getProvider(EntityManager.class).get();
		TypedQuery<AlertReceiver> query = entityManager
				.createNamedQuery("AlertReceiver.selectEnabled", AlertReceiver.class);
		List<AlertReceiver> recipients = query.getResultList();
		List<String> emails = new ArrayList<>();
		for (AlertReceiver recipient : recipients) {
			boolean candidate = false;
			switch (recipient.getLevel()) {
			case ALL:
			case ERROR:
				candidate = true;
				break;
			case WARNING:
				candidate = message.isWarning();
				break;
			case DEBUG:
				candidate = message.isDebug();
				break;
			default:
				break;
			}
			if (candidate) {
				emails.add(recipient.getEmail());
			}
		}
		for (String email : emails) {
			log.debug("Sending alert {} to {}", message.getMessage(), email);
			MailMessageConverter converter = new MailMessageConverter(message);
			String mailBody = converter.getBody();
			MailService mailService = injector.getInstance(MailService.class);
			try {
				mailService.createEmail(email, "biller", "Biller notification", mailBody)
						.send();
			}
			catch (Exception ex) {
				log.warn("Error sending notification", ex);
			}
		}
	}
}
