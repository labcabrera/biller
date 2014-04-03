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

	/**
	 * Obtiene el parametro pasado al {@link Job} a partir de su clave y tipo.
	 * 
	 * @param context
	 * @param key
	 * @param type
	 * @return
	 */
	protected <T> T getParameter(JobExecutionContext context, String key, Class<T> type) {
		return getParameter(context, key, type, null);
	}

	/**
	 * Obtiene el parametro pasado al {@link Job} a partir de su clave y tipo. En caso de no esté establecido en el contexto del job
	 * devuelve su valor por defecto.
	 * 
	 * @param context
	 * @param key
	 * @param type
	 * @param defaultValue
	 * @return
	 */
	@SuppressWarnings("unchecked")
	protected <T> T getParameter(JobExecutionContext context, String key, Class<T> type, T defaultValue) {
		Object result = context.get(key);
		if (context.get(key) != null) {
			return (T) result;
		} else {
			return defaultValue;
		}
	}

	/**
	 * Inicializa el job estableciendo el {@link Injector} de Guice.
	 * 
	 * @param context
	 */
	protected void init(JobExecutionContext context) {
		try {
			injector = (Injector) context.getScheduler().getContext().get(Injector.class.getName());
			Validate.notNull(injector);
		} catch (SchedulerException ex) {
			throw new RuntimeException();
		}
	}

	/**
	 * Establece el {@link Injector} de Guice.
	 * 
	 * @param value
	 */
	public void setInjector(Injector value) {
		this.injector = value;
	}
}
