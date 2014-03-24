/*******************************************************************************
 * Copyright (c) 2004, 2012 Kotasoft S.L.
 * All rights reserved. This program and the accompanying materials
 * may only be used prior written consent of Kotasoft S.L.
 ******************************************************************************/
package com.luckia.biller.core.scheduler;

import org.apache.commons.lang.Validate;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.SchedulerException;

import com.google.inject.Injector;

/**
 * Nota: al utilizar Quartz no tenemos control sobre como se generan las intancias de los objetos, de modo que no podemos utilizar el IoC de
 * Guice (recordar que estamos declarando los jobs a traves de las clases). Para solucionar esto estamos incluyendo directamente el
 * {@link Injector} de Guice en el contexto del scheduler.<br>
 * Para poder recuperar una instancia del IoC simplemente haremos:<br>
 * <b>MyClass myClass = injector.getInstance(MyClass.class)</b>
 */
public abstract class BaseJob implements Job {

	protected Injector injector;

	protected <T> T getParameter(JobExecutionContext context, String key, Class<T> type) {
		return getParameter(context, key, type, null);
	}

	@SuppressWarnings("unchecked")
	protected <T> T getParameter(JobExecutionContext context, String key, Class<T> type, T defaultValue) {
		Object result = context.get(key);
		if (context.get(key) != null) {
			return (T) result;
		} else {
			return defaultValue;
		}
	}

	protected void init(JobExecutionContext context) {
		try {
			injector = (Injector) context.getScheduler().getContext().get(Injector.class.getName());
			Validate.notNull(injector);
		} catch (SchedulerException ex) {
			throw new RuntimeException();
		}
	}

	public void setInjector(Injector value) {
		this.injector = value;
	}
}
