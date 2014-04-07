/*******************************************************************************
 * Copyright (c) 2004, 2012 Kotasoft S.L.
 * All rights reserved. This program and the accompanying materials
 * may only be used prior written consent of Kotasoft S.L.
 ******************************************************************************/
package com.luckia.biller.web.rest;

import javax.persistence.EntityManager;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.luckia.biller.core.ClearCache;
import com.luckia.biller.core.jpa.EntityManagerProvider;
import com.luckia.biller.core.model.AppFile;
import com.luckia.biller.core.model.Bill;
import com.luckia.biller.core.model.BillDetail;
import com.luckia.biller.core.model.common.Message;
import com.luckia.biller.core.model.common.SearchParams;
import com.luckia.biller.core.model.common.SearchResults;
import com.luckia.biller.core.services.FileService;
import com.luckia.biller.core.services.bills.impl.BillProcessorImpl;
import com.luckia.biller.core.services.entities.BillEntityService;
import com.luckia.biller.core.services.mail.MailService;
import com.luckia.biller.core.services.mail.SendMailTask;
import com.luckia.biller.core.services.pdf.PDFBillGenerator;

/**
 * Servicio REST que aparte del CRUD básico de facturas provee de las siguientes funcionalidades:
 * <ul>
 * <li>Aceptación de la factura</li>
 * <li>Rectificación de la factura</li>
 * <li>CRUD de detalles de facturación</li>
 * <li>Descarga del borrador de la factura</li>
 * <li>Envío de la factura por email</li>
 * </ul>
 */
@Path("bills")
public class BillRestService {

	private static final Logger LOG = LoggerFactory.getLogger(BillRestService.class);

	@Inject
	private EntityManagerProvider entityManagerProvider;
	@Inject
	private BillEntityService billService;
	@Inject
	private BillProcessorImpl billProcessor;
	@Inject
	private PDFBillGenerator pdfBillGenerator;
	@Inject
	private MailService mailService;
	@Inject
	private FileService fileService;

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/{id}")
	@ClearCache
	public Bill findById(@PathParam("id") String primaryKey) {
		return billService.findById(primaryKey);
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/find")
	@ClearCache
	public SearchResults<Bill> find(@QueryParam("n") Integer maxResults, @QueryParam("p") Integer page, @QueryParam("q") String queryString) {
		SearchParams params = new SearchParams();
		params.setItemsPerPage(maxResults);
		params.setCurrentPage(page);
		params.setQueryString(queryString);
		return billService.find(params);
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/merge")
	@ClearCache
	public Message<Bill> merge(Bill bill) {
		try {
			EntityManager entityManager = entityManagerProvider.get();
			Bill current = entityManager.find(Bill.class, bill.getId());
			current.merge(bill);
			entityManager.getTransaction().begin();
			entityManager.merge(current);
			entityManager.getTransaction().commit();
			return new Message<Bill>(Message.CODE_SUCCESS, "Factura actualizada", bill);
		} catch (Exception ex) {
			LOG.error("Error al actualizar la factura", ex);
			return new Message<Bill>(Message.CODE_GENERIC_ERROR, "Error al actualizar la factura");
		}
	}

	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Path("confirm/{id}")
	@ClearCache
	public Message<Bill> confirm(@PathParam("id") String id) {
		try {
			Bill bill = entityManagerProvider.get().find(Bill.class, id);
			billProcessor.confirmBill(bill);
			return new Message<Bill>(Message.CODE_SUCCESS, "Factura confirmada", bill);
		} catch (Exception ex) {
			LOG.error("Error al confirmar la factura", ex);
			return new Message<Bill>(Message.CODE_GENERIC_ERROR, "Error al confirmar la factura");
		}
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/detail/id/{id}")
	@ClearCache
	public BillDetail find(@PathParam("id") String id) {
		return entityManagerProvider.get().find(BillDetail.class, id);
	}


	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/detail/merge")
	@ClearCache
	public Message<Bill> merge(BillDetail detail) {
		try {
			Bill bill = billProcessor.mergeDetail(detail);
			return new Message<Bill>(Message.CODE_SUCCESS, "Detalle actualizado", bill);
		} catch (Exception ex) {
			LOG.error("Error al actualizar el detalle", ex);
			return new Message<Bill>(Message.CODE_GENERIC_ERROR, "Error al actualizar el detalle");
		}
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("/detail/remove/id/{id}")
	@ClearCache
	public Message<Bill> removeDetail(@PathParam("id") String id) {
		try {
			EntityManager entityManager = entityManagerProvider.get();
			BillDetail detail = entityManager.find(BillDetail.class, id);
			Bill bill = billProcessor.removeDetail(detail);
			return new Message<Bill>(Message.CODE_SUCCESS, "Detalle eliminado", bill);
		} catch (Exception ex) {
			LOG.error("Error al eliminar el detalle", ex);
			return new Message<Bill>(Message.CODE_GENERIC_ERROR, "Error al eliminar el detalle", null);
		}
	}

	/**
	 * Envia el PDF por correo a un destinatario.
	 * 
	 * @param id
	 * @param emailAddress
	 *            destinatarios de correo
	 * @return
	 */
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/send/{id}")
	@ClearCache
	public Message<Bill> sendEmail(@PathParam("id") String id, String emailAddress) {
		try {
			EntityManager entityManager = entityManagerProvider.get();
			entityManager.clear();
			Bill bill = entityManager.find(Bill.class, id);
			AppFile appFile = bill.getPdfFile();
			String title = "Factura " + bill.getCode();
			String body = "Adjunto PDF";
			SendMailTask task = new SendMailTask(emailAddress, appFile, title, body, fileService, mailService);
			new Thread(task).start();
			return new Message<Bill>(Message.CODE_SUCCESS, "Factura enviada por correo a " + emailAddress, bill);
		} catch (Exception ex) {
			LOG.error("Error al enviar la factura", ex);
			return new Message<Bill>(Message.CODE_SUCCESS, "Error al enviar la factura por correo");
		}
	}

	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/rectify/{id}")
	@ClearCache
	public Message<Bill> rectify(@PathParam("id") String id) {
		try {
			EntityManager entityManager = entityManagerProvider.get();
			entityManager.clear();
			Bill bill = entityManager.find(Bill.class, id);
			Bill rectified = billProcessor.rectifyBill(bill);
			return new Message<Bill>(Message.CODE_SUCCESS, "Factura enviada por correo", rectified);
		} catch (Exception ex) {
			LOG.error("Error al generar la rectificación", ex);
			return new Message<Bill>(Message.CODE_GENERIC_ERROR, "Error al generar la rectificación", null);
		}
	}

	@GET
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	@Path("/draft/{id}")
	@ClearCache
	public void getArtifactBinaryContent(@PathParam("id") String id, @Context HttpServletResponse response) {
		try {
			EntityManager entityManager = entityManagerProvider.get();
			entityManager.clear();
			Bill bill = entityManager.find(Bill.class, id);
			ServletOutputStream out = response.getOutputStream();
			pdfBillGenerator.generate(bill, out);
			response.setContentType("application/octet-stream");
			response.setHeader("Content-Disposition", String.format("attachment; filename=\"%s\"", "borrador.pdf"));
			response.setHeader("Content-Type", "application/pdf");
			out.flush();
		} catch (Exception ex) {
			LOG.error("Error al generar el borrador", ex);
			throw new RuntimeException("Error la generar el borrador");
		}
	}
}
