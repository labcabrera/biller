/*******************************************************************************
 * Copyright (c) 2004, 2012 Kotasoft S.L.
 * All rights reserved. This program and the accompanying materials
 * may only be used prior written consent of Kotasoft S.L.
 ******************************************************************************/
package com.luckia.biller.core.services.mail;

import java.util.List;

import org.apache.commons.mail.EmailAttachment;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.name.Named;

public class MailService {

	private static final Logger LOG = LoggerFactory.getLogger(MailService.class);

	@Inject
	@Named("mail.hostName")
	private String hostName;
	@Inject
	@Named("mail.emailUser")
	private String emailUser;
	@Inject
	@Named("mail.emailPassword")
	private String emailPassword;
	@Inject
	@Named("mail.sslConnection")
	private String sslConnection;
	@Inject
	@Named("mail.fromEmail")
	private String fromEmail;
	@Inject
	@Named("mail.fromName")
	private String fromName;
	@Inject(optional = true)
	@Named("mail.port")
	private Integer port;

	public HtmlEmail createEmail(String address, String displayName, String title, String body, List<EmailAttachment> attachments) throws EmailException {
		LOG.debug("Creando email con titulo {}", displayName);
		HtmlEmail email = new HtmlEmail();
		if (emailUser != null && !emailUser.equals("") && emailPassword != null && !emailPassword.equals("")) {
			email.setAuthentication(emailUser, emailPassword);
		}
		if (port != null) {
			email.setSmtpPort(port);
		}
		if (sslConnection != null && !sslConnection.equals("")) {
			email.setSSLOnConnect(Boolean.valueOf(sslConnection));
		}
		email.setHostName(hostName);
		email.addTo(address, displayName);
		email.setFrom(fromEmail, fromName);
		email.setSubject(title);
		if (attachments != null) {
			for (EmailAttachment attachment : attachments) {
				email.attach(attachment);
			}
		}
		email.setHtmlMsg(body);
		return email;
	}

	public static EmailAttachment createAttachment(String description, String name, String path) {
		EmailAttachment emailAttachment = new EmailAttachment();
		emailAttachment.setDescription(description);
		emailAttachment.setName(name);
		emailAttachment.setPath(path);
		return emailAttachment;
	}
}
