package com.luckia.biller.core.services.mail;

import java.util.List;

import org.apache.commons.mail.EmailAttachment;
import org.apache.commons.mail.HtmlEmail;
import org.junit.Ignore;
import org.junit.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.luckia.biller.core.BillerModule;
import com.luckia.biller.core.common.BillerException;

@Ignore
public class MailServiceTest {

	@Test
	public void test() {
		try {
			Injector injector = Guice.createInjector(new BillerModule());
			MailService mailService = injector.getInstance(MailService.class);
			String address = "lab.cabrera@gmail.com";
			String displayName = "displayName";
			String title = "Liquidación";
			String body = "Ejemplo con carácteres UNICODE como por ejemplo la palabra año";
			List<EmailAttachment> emailAttachments = null;
			HtmlEmail email = mailService.createEmail(address, displayName, title, body,
					emailAttachments);
			email.send();
		}
		catch (Exception ex) {
			ex.printStackTrace();
			throw new BillerException(ex);
		}
	}
}
