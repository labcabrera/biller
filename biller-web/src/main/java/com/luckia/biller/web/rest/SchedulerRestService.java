package com.luckia.biller.web.rest;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.quartz.CronScheduleBuilder;
import org.quartz.spi.MutableTrigger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.persist.Transactional;
import com.luckia.biller.core.model.ScheduledTask;
import com.luckia.biller.core.model.common.Message;
import com.luckia.biller.core.model.common.SearchResults;
import com.luckia.biller.core.scheduler.SchedulerService;

@Path("/scheduler")
@Consumes({ "application/json; charset=UTF-8" })
@Produces({ "application/json; charset=UTF-8" })
public class SchedulerRestService {

	private static final Logger LOG = LoggerFactory.getLogger(SchedulerRestService.class);

	@Inject
	private Provider<EntityManager> entityManagerProvider;
	@Inject
	private SchedulerService schedulerService;

	@GET
	@Path("/find")
	public SearchResults<ScheduledTask> search() {
		List<ScheduledTask> tasks = entityManagerProvider.get().createQuery("SchedulerTask.selectAll", ScheduledTask.class).getResultList();
		SearchResults<ScheduledTask> results = new SearchResults<>();
		results.setResults(tasks);
		return results;
	}

	@POST
	@Path("/merge")
	@Transactional
	public Message<ScheduledTask> merge(ScheduledTask task) {
		try {
			EntityManager entityManager = entityManagerProvider.get();
			ScheduledTask current = entityManager.find(ScheduledTask.class, task.getId());
			current.merge(task);
			schedulerService.unregisterTask(task);
			schedulerService.registerTask(task);
			entityManager.merge(current);
			return new Message<ScheduledTask>(Message.CODE_SUCCESS, "scheduler.task.merge.ok").withPayload(current);
		} catch (Exception ex) {
			LOG.error("Merge error", ex);
			return new Message<ScheduledTask>(Message.CODE_GENERIC_ERROR).addError("scheduler.task.merge.error");
		}
	}

	@POST
	@Path("/pause/{id}")
	@Transactional
	public Message<ScheduledTask> pause(@PathParam("id") String taskId) {
		try {
			EntityManager entityManager = entityManagerProvider.get();
			ScheduledTask current = entityManager.find(ScheduledTask.class, taskId);
			current.setEnabled(false);
			entityManager.merge(current);
			schedulerService.unregisterTask(current);
			return new Message<ScheduledTask>(Message.CODE_SUCCESS).withPayload(current).addInfo("scheduler.task.pause.ok");
		} catch (Exception ex) {
			LOG.error("Pause error", ex);
			return new Message<ScheduledTask>(Message.CODE_GENERIC_ERROR).addError("scheduler.task.pause.error");
		}
	}

	@POST
	@Path("/resume/{id}")
	@Transactional
	public Message<ScheduledTask> resume(@PathParam("id") String taskId) {
		try {
			EntityManager entityManager = entityManagerProvider.get();
			ScheduledTask current = entityManager.find(ScheduledTask.class, taskId);
			current.setEnabled(true);
			entityManager.merge(current);
			schedulerService.registerTask(current);
			return new Message<ScheduledTask>(Message.CODE_SUCCESS).withPayload(current).addInfo("scheduler.task.resume.ok");
		} catch (Exception ex) {
			LOG.error("Resume error", ex);
			return new Message<ScheduledTask>(Message.CODE_GENERIC_ERROR).addError("scheduler.task.resume.error");
		}
	}

	@POST
	@Path("/pauseAll")
	@Transactional
	public Message<ScheduledTask> pauseAll() {
		try {
			schedulerService.getScheduler().pauseAll();
			return new Message<ScheduledTask>(Message.CODE_SUCCESS).addInfo("scheduler.task.pauseAll.ok");
		} catch (Exception ex) {
			return new Message<ScheduledTask>(Message.CODE_GENERIC_ERROR).addError("scheduler.task.pauseAll.error");
		}
	}

	@POST
	@Path("/resumeAll")
	@Transactional
	public Message<ScheduledTask> resumeAll() {
		try {
			schedulerService.getScheduler().resumeAll();
			return new Message<ScheduledTask>(Message.CODE_SUCCESS).addInfo("scheduler.task.resumeAll.ok");
		} catch (Exception ex) {
			return new Message<ScheduledTask>(Message.CODE_GENERIC_ERROR).addError("scheduler.task.resumeAll.error");
		}
	}

	@POST
	@Path("/execute/{id}")
	@Transactional
	public Message<ScheduledTask> execute(@PathParam("id") String taskId) {
		try {
			EntityManager entityManager = entityManagerProvider.get();
			ScheduledTask task = entityManager.find(ScheduledTask.class, taskId);
			schedulerService.execute(task);
			return new Message<ScheduledTask>().addInfo("scheduler.task.execute.success");
		} catch (Exception ex) {
			LOG.error("Execute task error", ex);
			return new Message<ScheduledTask>(Message.CODE_GENERIC_ERROR).addError("scheduler.task.execute.error").addError(ex.getMessage());
		}
	}

	/**
	 * Metodo que devuelve un mensaje con las siguientes fechas de ejecucion de una tarea planificada
	 * 
	 * @param taskId
	 * @return
	 */
	@GET
	@Path("/nextExecutions/{id}")
	public Message<String> getNextExecutions(@PathParam("id") String taskId, @QueryParam("c") Integer count) {
		try {
			EntityManager entityManager = entityManagerProvider.get();
			ScheduledTask task = entityManager.find(ScheduledTask.class, taskId);
			if (StringUtils.isNotBlank(task.getCronExpression())) {
				Message<String> message = new Message<String>();
				CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(task.getCronExpression());
				MutableTrigger trigger = scheduleBuilder.build();
				Date checkDate = Calendar.getInstance().getTime();
				count = (count != null && count > 0) ? count : 5;
				for (int i = 0; i < count; i++) {
					Date targetDate = trigger.getFireTimeAfter(checkDate);
					message.addInfo(DateFormatUtils.ISO_DATETIME_FORMAT.format(targetDate));
					checkDate = targetDate;
				}
				return message;
			} else {
				return new Message<String>().addInfo("scheduler.nextExecutions.undefinedCronExpression");
			}
		} catch (Exception ex) {
			return new Message<String>(Message.CODE_GENERIC_ERROR).addError("scheduler.nextExecutions.error");
		}

	}
}
