/*******************************************************************************
 * Copyright (c) 2004, 2012 Kotasoft S.L.
 * All rights reserved. This program and the accompanying materials
 * may only be used prior written consent of Kotasoft S.L.
 ******************************************************************************/
package com.luckia.biller.web.rest;

import java.io.InputStream;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.persistence.EntityManager;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.luckia.biller.core.model.AppFile;
import com.luckia.biller.core.services.FileService;

@Path("/binary")
public class BinaryRestService {

	private static final Logger LOG = LoggerFactory.getLogger(BinaryRestService.class);

	@Inject
	private Provider<EntityManager> entityManagerProvider;
	@Inject
	private FileService fileService;

	@GET
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	@Path("download/{id}")
	public Response getArtifactBinaryContent(@PathParam("id") Long id) throws Exception {
		try {
			EntityManager entityManager = entityManagerProvider.get();
			AppFile appFile = entityManager.find(AppFile.class, id);
			Validate.notNull(appFile, "No se encuentra el contenido " + id);
			InputStream contentInputStream = fileService.getInputStream(appFile);
			ResponseBuilder response = Response.ok(contentInputStream);
			response.header("Content-Disposition", String.format("attachment; filename=\"%s\"", appFile.getName()));
			response.header("Content-Type", appFile.getContentType());
			return response.build();
		} catch (Exception ex) {
			LOG.error(ex.getMessage(), ex);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

}
