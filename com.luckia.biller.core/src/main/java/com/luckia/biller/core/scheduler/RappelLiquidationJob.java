package com.luckia.biller.core.scheduler;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.persistence.EntityManager;

import org.apache.commons.lang3.Range;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.joda.time.DateTime;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.luckia.biller.core.scheduler.tasks.RappelLiquidationTask;

public class RappelLiquidationJob extends BaseJob {

	public static final String KEY_THREAD_COUNT = "thread.count";
	private static final Logger LOG = LoggerFactory.getLogger(RappelLiquidationJob.class);

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.quartz.Job#execute(org.quartz.JobExecutionContext)
	 */
	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		Date currentDate = Calendar.getInstance().getTime();
		Integer threadCount = getParameter(context, KEY_THREAD_COUNT, Integer.class, 10);
		execute(currentDate, threadCount);
	}

	public void execute(Date date, int threadCount) {
		DateTime dateTime = new DateTime(date);
		DateTime from = null;
		if (dateTime.getMonthOfYear() == 12) {
			from = new DateTime(dateTime).dayOfYear().withMinimumValue();
		} else if (dateTime.getMonthOfYear() == 1) {
			from = new DateTime(dateTime).plusYears(-1).dayOfYear().withMinimumValue();
		}
		if (from == null) {
			LOG.debug("No es necesario ejecutar el calculo de rappel a fecha {}", DateFormatUtils.ISO_DATE_FORMAT.format(date));
		} else {
			DateTime to = from != null ? new DateTime(from).dayOfYear().withMaximumValue() : null;
			execute(Range.between(from.toDate(), to.toDate()), threadCount);
		}
	}

	public void execute(Range<Date> range, int threadCount) {
		EntityManager entityManager = injector.getProvider(EntityManager.class).get();
		long t0 = System.currentTimeMillis();
		ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
		List<Long> storeIds = entityManager.createQuery("select s.id from Store s order by s.name", Long.class).getResultList();
		for (Long storeId : storeIds) {
			RappelLiquidationTask task = new RappelLiquidationTask(storeId, range, injector);
			executorService.submit(task);
		}
		LOG.debug("Esperando a la finalizacion de {} tareas de rappel (hilos: {})", storeIds.size(), threadCount);
		executorService.shutdown();
		try {
			executorService.awaitTermination(4, TimeUnit.HOURS);
			LOG.debug("Finalizadas {} tareas en {} ms", storeIds.size(), (System.currentTimeMillis() - t0));
		} catch (InterruptedException ex) {
			LOG.error("Error durante la ejecucion de las tareas", ex);
		}
	}
}
