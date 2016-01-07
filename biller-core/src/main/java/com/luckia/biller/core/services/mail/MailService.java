package com.luckia.biller.core.services.mail;

import java.util.List;
import java.util.Properties;

import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.mail.EmailAttachment;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.luckia.biller.core.common.SettingsManager;

@Singleton
public class MailService {

	private static final Logger LOG = LoggerFactory.getLogger(MailService.class);

	// @Inject
	// private SettingsService settingsService;
	@Inject
	private SettingsManager settingsManager;

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
		Properties properties = settingsManager.getProperties("mail");
		hostName = properties.getProperty("host");
		emailUser = properties.getProperty("user");
		emailPassword = properties.getProperty("password");
		fromEmail = properties.getProperty("fromEmail");
		sslConnection = properties.getProperty("sslConnection");
		tlsConnection = properties.getProperty("tlsConnection");
		port = Integer.parseInt(properties.getProperty("port"));
	}

	/**
	 * @param address
	 * @param displayName
	 * @param title
	 * @param body
	 * @return
	 * @throws EmailException
	 */
	public HtmlEmail createEmail(String address, String displayName, String title, String body) throws EmailException {
		return createEmail(address, displayName, title, body, null);
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
	public HtmlEmail createEmail(String address, String displayName, String title, String body,
			List<EmailAttachment> attachments) throws EmailException {
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
