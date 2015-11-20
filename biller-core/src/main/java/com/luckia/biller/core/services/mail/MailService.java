package com.luckia.biller.core.services.mail;

import java.util.List;

import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.mail.EmailAttachment;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.luckia.biller.core.model.AppSettings;
import com.luckia.biller.core.services.SettingsService;

@Singleton
public class MailService {

	private static final Logger LOG = LoggerFactory.getLogger(MailService.class);

	@Inject
	private SettingsService settingsService;

	private String hostName;
	private String emailUser;
	private String emailPassword;
	private String sslConnection;
	private String tlsConnection;
	private String fromEmail;
	private String fromName;
	private Integer port;

	public void init() {
		LOG.debug("Loading mail settings");
		AppSettings appSettings = settingsService.getMailSettings();
		hostName = appSettings.getValue("hostName", String.class);
		emailUser = appSettings.getValue("emailUser", String.class);
		emailPassword = appSettings.getValue("emailPassword", String.class);
		fromEmail = appSettings.getValue("fromEmail", String.class);
		sslConnection = appSettings.getValue("sslConnection", String.class);
		tlsConnection = appSettings.getValue("tlsConnection", String.class);
		port = Integer.parseInt(appSettings.getValue("port", String.class));
	}

	/**
	 * Genera un objeto {@link HtmlEmail}
	 * 
	 * @param address
	 * @param displayName
	 * @param title
	 * @param body
	 * @param attachments
	 * @return
	 * @throws EmailException
	 */
	public HtmlEmail createEmail(String address, String displayName, String title, String body, List<EmailAttachment> attachments) throws EmailException {
		init();
		LOG.debug("Creando email con titulo {}", displayName);
		HtmlEmail email = new HtmlEmail();
		if (emailUser != null && !emailUser.equals("") && emailPassword != null && !emailPassword.equals("")) {
			email.setAuthentication(emailUser, emailPassword);
		}
		if (port != null) {
			email.setSmtpPort(port);
		}
		if (StringUtils.isNotBlank(tlsConnection) && Boolean.valueOf(tlsConnection)) {
			email.setStartTLSRequired(true);
		} else if (StringUtils.isNotBlank(sslConnection) && Boolean.valueOf(sslConnection)) {
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

	public EmailAttachment createAttachment(String description, String name, String path) {
		EmailAttachment emailAttachment = new EmailAttachment();
		emailAttachment.setDescription(description);
		emailAttachment.setName(name);
		emailAttachment.setPath(path);
		return emailAttachment;
	}
}
