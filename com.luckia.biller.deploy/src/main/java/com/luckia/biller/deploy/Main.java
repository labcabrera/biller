package com.luckia.biller.deploy;

import java.util.Date;

import org.apache.commons.lang3.Range;
import org.joda.time.DateTime;
import org.quartz.JobExecutionException;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.luckia.biller.core.MainModule;
import com.luckia.biller.core.scheduler.BillingJob;
import com.luckia.biller.deploy.poi.MasterWorkbookProcessor;

/**
 * Clase encargada de realizar la carga inicial de base de datos.
 */
public class Main implements Runnable {

	public static void main(String[] args) {
		new Main().run();
	}

	private Injector injector;

	@Override
	public void run() {
		try {
			System.out.println("Biller deployment module");
			Bootstrap.main();
			MasterWorkbookProcessor.main();
			injector = Guice.createInjector(new MainModule());
			injector.getInstance(BillSequencePrefixGenerator.class).run();
			generateBills();
			updateEgasaInfo();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * DESTINATARIO: Egasa Hattrick S.A. Arzobispo Fabian y Fuero 17B 46009 Valencia España Tel: 961100793 NIF: A98359169
	 */
	private void updateEgasaInfo() {
		// TODO

	}

	private void generateBills() throws JobExecutionException {
		System.out.println("Generando facturacion de enero");
		BillingJob job = injector.getInstance(BillingJob.class);
		job.setInjector(injector);
		Date from = new DateTime(2014, 1, 1, 0, 0, 0, 0).toDate();
		Date to = new DateTime(2014, 1, 31, 0, 0, 0, 0).toDate();
		job.execute(Range.between(from, to), 10);
	}
}
