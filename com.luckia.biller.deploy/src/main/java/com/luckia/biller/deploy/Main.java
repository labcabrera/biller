package com.luckia.biller.deploy;

import java.io.IOException;
import java.util.Date;

import org.apache.commons.lang3.Range;
import org.joda.time.DateTime;
import org.quartz.JobExecutionException;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.luckia.biller.core.MainModule;
import com.luckia.biller.core.scheduler.BillerJob;
import com.luckia.biller.deploy.poi.MasterWorkbookProcessor;

/**
 * Clase encargada de realizar la carga inicial de base de datos.
 */
public class Main {

	public static void main(String[] args) throws IOException, JobExecutionException {
		System.out.println("Biller deployment module");
		Bootstrap.main(args);
		MasterWorkbookProcessor.main(args);

		Injector injector = Guice.createInjector(new MainModule());
		BillerJob job = injector.getInstance(BillerJob.class);
		job.setInjector(injector);

		Date from = new DateTime(2014, 1, 1, 0, 0, 0, 0).toDate();
		Date to = new DateTime(2014, 1, 31, 0, 0, 0, 0).toDate();
		job.execute(Range.between(from, to), 10);
	}
}
