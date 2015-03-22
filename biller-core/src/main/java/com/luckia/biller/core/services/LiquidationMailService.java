package com.luckia.biller.core.services;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import javax.inject.Inject;

import com.luckia.biller.core.i18n.I18nService;
import com.luckia.biller.core.model.CommonState;
import com.luckia.biller.core.model.Liquidation;
import com.luckia.biller.core.services.mail.MailService;
import com.luckia.biller.core.services.mail.SendLocalFileMailTask;

public class LiquidationMailService {

	@Inject
	private ZipFileService zipFileService;
	@Inject
	private I18nService i18nService;
	@Inject
	private MailService mailService;
	@Inject
	private StateMachineService stateMachineService;

	public void sendEmail(Liquidation liquidation, String emailAddress, boolean updateState) {
		try {
			String title = "liquidacion.zip";
			DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
			String sender = liquidation.getSender().getName();
			String body = String.format(i18nService.getMessage("mail.liquidation.body"), sender, df.format(liquidation.getDateFrom()), df.format(liquidation.getDateTo()));
			Boolean deleteOnExit = true;
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			zipFileService.generate(liquidation, out);
			File tmpFile = File.createTempFile("biller-liquidation-", ".tmp");
			tmpFile.deleteOnExit();
			FileOutputStream fileOut = new FileOutputStream(tmpFile);
			fileOut.write(out.toByteArray());
			fileOut.flush();
			fileOut.close();
			SendLocalFileMailTask task = new SendLocalFileMailTask(emailAddress, tmpFile.getAbsolutePath(), title, title, body, mailService, deleteOnExit);
			new Thread(task).start();
			if (updateState) {
				stateMachineService.createTransition(liquidation, CommonState.Sent.name());
			}
		} catch (Exception ex) {
			throw new RuntimeException("Error durante el envio del correo", ex);
		}
	}
}
