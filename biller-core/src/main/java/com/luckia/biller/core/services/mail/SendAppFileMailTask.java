package com.luckia.biller.core.services.mail;

import java.util.Arrays;

import org.apache.commons.mail.EmailAttachment;
import org.apache.commons.mail.HtmlEmail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.luckia.biller.core.model.AppFile;
import com.luckia.biller.core.services.FileService;

public class SendAppFileMailTask implements Runnable {

	private final Logger LOG = LoggerFactory.getLogger(SendAppFileMailTask.class);

	private final String emailAddress;
	private final AppFile appFile;
	private final String title;
	private final String body;
	private final FileService fileService;
	private final MailService mailService;

	public SendAppFileMailTask(String emailAddress, AppFile appFile, String title,
			String body, FileService fileService, MailService mailService) {
		this.emailAddress = emailAddress;
		this.appFile = appFile;
		this.title = title;
		this.body = body;
		this.fileService = fileService;
		this.mailService = mailService;
	}

	@Override
	public void run() {
		try {
			String filePath = fileService.getFilePath(appFile);
			EmailAttachment attachment = mailService.createAttachment(appFile.getName(),
					appFile.getName(), filePath);
			HtmlEmail email = mailService.createEmail(emailAddress, emailAddress, title,
					body, Arrays.asList(attachment));
			email.send();
		}
		catch (Exception ex) {
			LOG.error("Error al enviar el correo", ex);
		}
	}

}
