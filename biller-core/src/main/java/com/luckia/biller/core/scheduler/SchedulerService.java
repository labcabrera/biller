package com.luckia.biller.core.scheduler;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.apache.commons.lang3.StringUtils;
import org.quartz.CronScheduleBuilder;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.spi.MutableTrigger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Injector;
import com.luckia.biller.core.model.ScheduledTask;

/**
 * Al crear la instancia inserta en el contexto el {@link Injector} a partir del cual podremos utilizar los servicios.
 */
@Singleton
public class SchedulerService {

	private static final Logger LOG = LoggerFactory.getLogger(SchedulerService.class);

	private final Scheduler scheduler;
	private final Provider<EntityManager> entityManagerProvider;
	private final ClassLoader classLoader;

	/**
	 * Constructor de la clase que genera la instancia del Scheduler de Quartz.
	 * 
	 * @param injector
	 */
	@Inject
	public SchedulerService(Injector injector) {
		LOG.info("Starting scheduler service");
		try {
			Properties properties = new Properties();
			properties.load(getClass().getResourceAsStream("/org/quartz/quartz.properties"));
			scheduler = new StdSchedulerFactory(properties).getScheduler();
			scheduler.getContext().put(Injector.class.getName(), injector);
			entityManagerProvider = injector.getProvider(EntityManager.class);
			classLoader = Thread.currentThread().getContextClassLoader();
		} catch (Exception ex) {
			throw new RuntimeException("Error starting scheduler service", ex);
		}
	}

	/**
	 * Podemos consultar la aplicación <a href="http://www.cronmaker.com/">cronmaker.com</a> para generar las expresiones cron.
	 * 
	 * @throws SchedulerException
	 */
	@SuppressWarnings("unchecked")
	public void registerJobs() throws SchedulerException {
		LOG.debug("Registering scheduled jobs");
		try {
			EntityManager entityManager = entityManagerProvider.get();
			TypedQuery<ScheduledTask> query = entityManager.createNamedQuery("ScheduledTask.selectEnabled", ScheduledTask.class);
			List<ScheduledTask> tasks = query.getResultList();
			LOG.debug("Readed {} tasks from database", tasks.size());
			for (ScheduledTask task : tasks) {
				if (StringUtils.isNotBlank(task.getCronExpression())) {
					try {
						Class<? extends Job> taskClass = (Class<? extends Job>) task.getExecutorClass();
						registerJob(taskClass, task.getCronExpression(), task.getParams());
					} catch (Exception ex) {
						LOG.error("Error registering task " + task, ex);
					}
				}
			}
		} catch (Exception ex) {
			throw new RuntimeException("Error registering scheduled tasks", ex);
		}
	}

	/**
	 * Registra una tarea para ser ejecutada a partir de su expresión cron.
	 * 
	 * @param task
	 * @throws SchedulerException
	 * @throws ClassNotFoundException
	 */
	@SuppressWarnings("unchecked")
	public void registerTask(ScheduledTask task) throws SchedulerException, ClassNotFoundException {
		LOG.debug("Registering task {}", task);
		Class<? extends Job> jobClass = (Class<? extends Job>) classLoader.loadClass(task.getExecutorClass().getName());
		registerJob(jobClass, task.getCronExpression());
	}

	/**
	 * Elimina una tarea de la lista de tareas pendientes.
	 * 
	 * @param task
	 * @throws SchedulerException
	 */
	public void unregisterTask(ScheduledTask task) throws SchedulerException {
		LOG.debug("Removing task {}", task);
		JobKey jobKey = new JobKey(task.getExecutorClass().getName());
		scheduler.deleteJob(jobKey);
	}

	/**
	 * Registra una tarea programada a partir de la expresión cron.
	 */
	private void registerJob(Class<? extends Job> jobClass, String cronExpression) throws SchedulerException {
		registerJob(jobClass, cronExpression, null);
	}

	/**
	 * Registra una tarea programada.
	 * 
	 * @param jobClass
	 * @param cronExpression
	 * @param params
	 * @throws SchedulerException
	 */
	private void registerJob(Class<? extends Job> jobClass, String cronExpression, Map<String, String> params) throws SchedulerException {
		LOG.debug("Registering task {} with cron expression {}", jobClass.getName(), cronExpression);
		if (params != null) {
			for (Entry<String, String> i : params.entrySet()) {
				String key = i.getKey();
				String contextKey = jobClass.getName() + "." + key;
				scheduler.getContext().put(contextKey, i.getValue());
			}
		}
		String name = jobClass.getName();
		JobDetail jobDetail = JobBuilder.newJob(jobClass).withDescription(name).withIdentity(name).build();
		CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(cronExpression);
		MutableTrigger trigger = scheduleBuilder.build();
		trigger.setKey(new TriggerKey(name));
		scheduler.scheduleJob(jobDetail, trigger);
	}

	/**
	 * Ejecuta una única vez una determinada tarea programada.
	 * 
	 * @param task
	 * @throws SchedulerException
	 * @throws ClassNotFoundException
	 */
	@SuppressWarnings("unchecked")
	public void execute(ScheduledTask task) throws SchedulerException, ClassNotFoundException {
		LOG.debug("Executing task {}", task);
		Class<? extends Job> jobClass = (Class<? extends Job>) task.getExecutorClass();
		String name = String.format("%s_%s", System.currentTimeMillis(), jobClass.getName());
		JobDetail jobDetail = JobBuilder.newJob(jobClass).withDescription(name).withIdentity(name).build();
		SimpleScheduleBuilder builder = SimpleScheduleBuilder.simpleSchedule().withIntervalInSeconds(1).withRepeatCount(0);
		Trigger trigger = TriggerBuilder.newTrigger().withIdentity("Trigger_" + name).withSchedule(builder).build();
		scheduler.scheduleJob(jobDetail, trigger);
	}

	/**
	 * Obtiene la instancia de Scheduler de Quartz.
	 * 
	 * @return
	 */
	public Scheduler getScheduler() {
		return scheduler;
	}
}
