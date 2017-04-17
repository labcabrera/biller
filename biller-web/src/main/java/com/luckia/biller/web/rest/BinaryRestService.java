package com.luckia.biller.web.rest;

import java.io.InputStream;

import javax.annotation.security.PermitAll;
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

import com.luckia.biller.core.model.AppFile;
import com.luckia.biller.core.services.FileService;

import lombok.extern.slf4j.Slf4j;

/**
 * Servicio de descarga de ficheros.
 */
@Path("/binary")
@Slf4j
public class BinaryRestService {

	@Inject
	private Provider<EntityManager> entityManagerProvider;
	@Inject
	private FileService fileService;

	@GET
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	@PermitAll
	@Path("download/{id}")
	public Response getArtifactBinaryContent(@PathParam("id") Long id) {
		try {
			EntityManager entityManager = entityManagerProvider.get();
			AppFile appFile = entityManager.find(AppFile.class, id);
			Validate.notNull(appFile, "No se encuentra el contenido " + id);
			InputStream contentInputStream = fileService.getInputStream(appFile);
			ResponseBuilder response = Response.ok(contentInputStream);
			response.header("Content-Disposition",
					String.format("attachment; filename=\"%s\"", appFile.getName()));
			response.header("Content-Type", appFile.getContentType());
			return response.build();
		}
		catch (Exception ex) {
			log.error(ex.getMessage(), ex);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

}
