/*******************************************************************************
 * Copyright (c) 2004, 2012 Kotasoft S.L.
 * All rights reserved. This program and the accompanying materials
 * may only be used prior written consent of Kotasoft S.L.
 ******************************************************************************/
package com.luckia.biller.web.rest;

import java.io.DataInputStream;
import java.io.InputStream;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.apache.commons.lang.Validate;

import com.luckia.biller.core.jpa.EntityManagerProvider;
import com.luckia.biller.core.model.AppFile;
import com.luckia.biller.core.services.FileService;

@Path("binary")
public class BinaryRestService {

	@Inject
	private EntityManagerProvider entityManagerProvider;
	@Inject
	private FileService fileService;

	@GET
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	@Path("download/{id}")
	public void getArtifactBinaryContent(@PathParam("id") Long id, @Context HttpServletResponse response) throws Exception {
		try {
			EntityManager entityManager = entityManagerProvider.get();
			AppFile appFile = entityManager.find(AppFile.class, id);
			Validate.notNull(appFile, "No se encuentra el contenido " + id);
			InputStream contentInputStream = fileService.getInputStream(appFile);
			response.setContentType("application/octet-stream");
			// response.setContentLength(fLength);
			response.setHeader("Content-Disposition", String.format("attachment; filename=\"%s\"", appFile.getName()));
			response.setHeader("Content-Type", appFile.getContentType());
			ServletOutputStream sos = response.getOutputStream();
			byte[] bbuf = new byte[1024];
			DataInputStream in = new DataInputStream(contentInputStream);
			int length = 0;
			while ((in != null) && ((length = in.read(bbuf)) != -1)) {
				sos.write(bbuf, 0, length);
			}
			in.close();
			sos.flush();
			sos.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
