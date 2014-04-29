/*******************************************************************************
 * Copyright (c) 2004, 2012 Kotasoft S.L.
 * All rights reserved. This program and the accompanying materials
 * may only be used prior written consent of Kotasoft S.L.
 ******************************************************************************/
package com.luckia.biller.web.rest;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import javax.persistence.EntityManager;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.luckia.biller.core.ClearCache;
import com.luckia.biller.core.i18n.I18nService;
import com.luckia.biller.core.jpa.EntityManagerProvider;
import com.luckia.biller.core.model.Liquidation;
import com.luckia.biller.core.model.LiquidationDetail;
import com.luckia.biller.core.model.common.Message;
import com.luckia.biller.core.model.common.SearchParams;
import com.luckia.biller.core.model.common.SearchResults;
import com.luckia.biller.core.services.ZipFileService;
import com.luckia.biller.core.services.bills.LiquidationProcessor;
import com.luckia.biller.core.services.entities.LiquidationEntityService;
import com.luckia.biller.core.services.mail.MailService;
import com.luckia.biller.core.services.mail.SendLocalFileMailTask;
import com.luckia.biller.core.services.pdf.PDFLiquidationGenerator;

/**
 * Servio REST asociado a las liquidaciones.
 */
@Path("rest/liquidations")
public class LiquidationRestService {

	private static final Logger LOG = LoggerFactory.getLogger(LiquidationRestService.class);

	@Inject
	private LiquidationEntityService entityService;
	@Inject
	private LiquidationProcessor liquidationProcessor;
	@Inject
	private EntityManagerProvider entityManagerProvider;
	@Inject
	private I18nService i18nService;
	@Inject
	private PDFLiquidationGenerator pdfLiquidationGenerator;
	@Inject
	private ZipFileService zipFileService;
	@Inject
	private MailService mailService;

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/id/{id}")
	@ClearCache
	public Liquidation findById(@PathParam("id") String primaryKey) {
		return entityService.findById(primaryKey);
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/find")
	@ClearCache
	public SearchResults<Liquidation> find(@QueryParam("n") Integer maxResults, @QueryParam("p") Integer page, @QueryParam("q") String queryString) {
		SearchParams params = new SearchParams();
		params.setItemsPerPage(maxResults);
		params.setCurrentPage(page);
		params.setQueryString(queryString);
		return entityService.find(params);
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/merge")
	@ClearCache
	public Message<Liquidation> merge(Liquidation bill) {
		try {
			EntityManager entityManager = entityManagerProvider.get();
			Liquidation current = entityManager.find(Liquidation.class, bill.getId());
			current.merge(bill);
			entityManager.getTransaction().begin();
			entityManager.merge(current);
			entityManager.getTransaction().commit();
			return new Message<>(Message.CODE_SUCCESS, "Liquidación actualizada", bill);
		} catch (Exception ex) {
			LOG.error("Error al actualizar la factura", ex);
			return new Message<>(Message.CODE_GENERIC_ERROR, "Error al actualizar la liquidación");
		}
	}

	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Path("confirm/{id}")
	@ClearCache
	public Message<Liquidation> confirm(@PathParam("id") String id) {
		try {
			Liquidation liquidation = entityManagerProvider.get().find(Liquidation.class, id);
			liquidationProcessor.confirm(liquidation);
			return new Message<>(Message.CODE_SUCCESS, i18nService.getMessage("liquidation.confirm.success"), liquidation);
		} catch (Exception ex) {
			LOG.error("Error al confirmar la liquidacion", ex);
			return new Message<>(Message.CODE_GENERIC_ERROR, i18nService.getMessage("liquidation.confirm.error"));
		}
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/detail/id/{id}")
	@ClearCache
	public LiquidationDetail mergeDetail(@PathParam("id") String id) {
		return entityManagerProvider.get().find(LiquidationDetail.class, id);
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/detail/merge")
	@ClearCache
	public Message<Liquidation> mergeDetail(LiquidationDetail detail) {
		try {
			Liquidation liquidation = liquidationProcessor.mergeDetail(detail);
			return new Message<>(Message.CODE_SUCCESS, "Detalle actualizado", liquidation);
		} catch (Exception ex) {
			LOG.error("Error al confirmar la liquidacion", ex);
			return new Message<>(Message.CODE_GENERIC_ERROR, "Error al confirmar la liquidacion");
		}
	}

	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/detail/remove/{id}")
	@ClearCache
	public Message<Liquidation> removeDetail(@PathParam("id") String id) {
		try {
			LiquidationDetail detail = entityManagerProvider.get().find(LiquidationDetail.class, id);
			Liquidation liquidation = liquidationProcessor.removeDetail(detail);
			return new Message<>(Message.CODE_SUCCESS, "Detalle actualizado", liquidation);
		} catch (Exception ex) {
			LOG.error("Error al confirmar la liquidacion", ex);
			return new Message<>(Message.CODE_GENERIC_ERROR, "Error al confirmar la liquidacion");
		}
	}

	@GET
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	@Path("/draft/{id}")
	@ClearCache
	public Response getArtifactBinaryContent(@PathParam("id") String id) {
		try {
			EntityManager entityManager = entityManagerProvider.get();
			Liquidation liquidation = entityManager.find(Liquidation.class, id);
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			pdfLiquidationGenerator.generate(liquidation, out);
			ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
			ResponseBuilder response = Response.ok(in);
			response.header("Content-Disposition", String.format("attachment; filename=\"%s\"", "borrador.pdf"));
			response.header("Content-Type", "application/pdf");
			return response.build();
		} catch (Exception ex) {
			LOG.error("Error al generar el borrador", ex);
			throw new RuntimeException("Error la generar el borrador");
		}
	}

	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/send/{id}")
	@ClearCache
	public Message<Liquidation> sendEmail(@PathParam("id") String id, String emailAddress) {
		try {
			EntityManager entityManager = entityManagerProvider.get();
			entityManager.clear();
			Liquidation liquidation = entityManager.find(Liquidation.class, id);
			String title = "liquidacion.zip";
			DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
			String sender = liquidation.getSender().getName();
			String body = String.format(i18nService.getMessage("mail.liquidation.body"), sender, df.format(liquidation.getDateFrom()), df.format(liquidation.getDateTo()));
			Boolean deleteOnExit = true;
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			zipFileService.generate(liquidation, out);
			File tmpFile = File.createTempFile("biller-liquidation-", ".tmp");
			tmpFile.deleteOnExit();
			FileOutputStream fileOut = new FileOutputStream(tmpFile);
			fileOut.write(out.toByteArray());
			fileOut.flush();
			fileOut.close();
			SendLocalFileMailTask task = new SendLocalFileMailTask(emailAddress, tmpFile.getAbsolutePath(), title, title, body, mailService, deleteOnExit);
			new Thread(task).start();
			return new Message<>(Message.CODE_SUCCESS, String.format(i18nService.getMessage("liquidation.send.email.success"), emailAddress), liquidation);
		} catch (Exception ex) {
			LOG.error("Error al enviar la factura", ex);
			return new Message<>(Message.CODE_SUCCESS, i18nService.getMessage("liquidation.send.email.error"));
		}
	}
}
