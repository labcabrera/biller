package com.luckia.biller.web.servlet;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.persist.UnitOfWork;

@Singleton
@Provider
public final class RestPersistFilter implements ContainerRequestFilter, ContainerResponseFilter {

	private static final Logger LOG = LoggerFactory.getLogger(RestPersistFilter.class);

	private final UnitOfWork unitOfWork;

	@Inject
	public RestPersistFilter(UnitOfWork unitOfWork) {
		this.unitOfWork = unitOfWork;
	}

	@Override
	public void filter(ContainerRequestContext requestContext) throws IOException {
		try {
			unitOfWork.begin();
		} catch (Exception ex) {
			LOG.debug("La unidad de persistencia ya esta inicializada", ex);
			try {
				unitOfWork.end();
				unitOfWork.begin();
			} catch (Exception ignore) {
			}
		}
	}

	@Override
	public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
		try {
			unitOfWork.end();
		} catch (Exception ignore) {
		}
	}
}