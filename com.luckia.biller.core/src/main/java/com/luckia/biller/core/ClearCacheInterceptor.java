package com.luckia.biller.core;

import javax.inject.Inject;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.luckia.biller.core.jpa.EntityManagerProvider;

/**
 * {@link MethodInterceptor} encargado de limpiar la cache de resultados de JPA. Este metodo invalida las sesiones JPA cuando hay varios
 * hilos de ejecucion que modifican una entidad para evitar devolver resultados incorrectos.
 * 
 * @see ClearCache
 */
public class ClearCacheInterceptor implements MethodInterceptor {

	private static final Logger LOG = LoggerFactory.getLogger(ClearCacheInterceptor.class);

	@Inject
	private EntityManagerProvider entityManagerProvider;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.aopalliance.intercept.MethodInterceptor#invoke(org.aopalliance.intercept.MethodInvocation)
	 */
	@Override
	public Object invoke(MethodInvocation methodInvocation) throws Throwable {
		LOG.trace("Cleaning JPA local cache");
		entityManagerProvider.get().clear();
		return methodInvocation.proceed();
	}

}
