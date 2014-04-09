/*******************************************************************************
 * Copyright (c) 2004, 2012 Kotasoft S.L.
 * All rights reserved. This program and the accompanying materials
 * may only be used prior written consent of Kotasoft S.L.
 ******************************************************************************/
package com.luckia.biller.core.services.mail;

import java.util.List;

import org.apache.commons.mail.EmailAttachment;
import org.apache.commons.mail.HtmlEmail;
import org.junit.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.luckia.biller.core.LuckiaCoreModule;

public class MailServiceTest {

	@Test
	public void test() {
		try {
			Injector injector = Guice.createInjector(new LuckiaCoreModule());
			MailService mailService = injector.getInstance(MailService.class);
			String address = "lab.cabrera@gmail.com";
			String displayName = "displayName";
			String title = "title";
			String body = "body";
			List<EmailAttachment> emailAttachments = null;
			HtmlEmail email = mailService.createEmail(address, displayName, title, body, emailAttachments);
			email.send();
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new RuntimeException(ex);
		}
	}
}
