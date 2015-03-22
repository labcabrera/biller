package com.luckia.biller.core.services.mail;

import java.io.File;
import java.util.Arrays;

import org.apache.commons.mail.EmailAttachment;
import org.apache.commons.mail.HtmlEmail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SendLocalFileMailTask implements Runnable {

	private final Logger LOG = LoggerFactory.getLogger(SendLocalFileMailTask.class);

	private final String emailAddress;
	private final String filePath;
	private final String name;
	private final String title;
	private final String body;
	private final MailService mailService;
	private final Boolean deleteOnExit;

	public SendLocalFileMailTask(String emailAddress, String filePath, String name, String title, String body, MailService mailService, Boolean deleteOnExit) {
		this.emailAddress = emailAddress;
		this.filePath = filePath;
		this.name = name;
		this.title = title;
		this.body = body;
		this.mailService = mailService;
		this.deleteOnExit = deleteOnExit;
	}

	@Override
	public void run() {
		try {
			EmailAttachment attachment = mailService.createAttachment(name, name, filePath);
			HtmlEmail email = mailService.createEmail(emailAddress, emailAddress, title, body, Arrays.asList(attachment));
			email.send();
			if (deleteOnExit != null && deleteOnExit) {
				deleteOnExit();
			}
		} catch (Exception ex) {
			LOG.error("Error al enviar el correo", ex);
		}
	}

	private void deleteOnExit() {
		try {
			File file = new File(filePath);
			file.delete();
		} catch (Exception ignore) {

		}
	}
}
