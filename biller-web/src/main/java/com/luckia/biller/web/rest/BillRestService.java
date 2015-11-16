/*******************************************************************************
 * Copyright (c) 2004, 2012 Kotasoft S.L.
 * All rights reserved. This program and the accompanying materials
 * may only be used prior written consent of Kotasoft S.L.
 ******************************************************************************/
package com.luckia.biller.web.rest;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import javax.inject.Provider;
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

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import com.luckia.biller.core.i18n.I18nService;
import com.luckia.biller.core.model.AppFile;
import com.luckia.biller.core.model.Bill;
import com.luckia.biller.core.model.BillDetail;
import com.luckia.biller.core.model.BillLiquidationDetail;
import com.luckia.biller.core.model.CommonState;
import com.luckia.biller.core.model.common.Message;
import com.luckia.biller.core.model.common.SearchParams;
import com.luckia.biller.core.model.common.SearchResults;
import com.luckia.biller.core.services.FileService;
import com.luckia.biller.core.services.StateMachineService;
import com.luckia.biller.core.services.bills.impl.BillProcessorImpl;
import com.luckia.biller.core.services.bills.recalculation.BillRecalculationService;
import com.luckia.biller.core.services.entities.BillEntityService;
import com.luckia.biller.core.services.mail.MailService;
import com.luckia.biller.core.services.mail.SendAppFileMailTask;
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
@Path("/bills")
public class BillRestService {

	private static final Logger LOG = LoggerFactory.getLogger(BillRestService.class);

	@Inject
	private Provider<EntityManager> entityManagerProvider;
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
	@Inject
	private I18nService i18nService;
	@Inject
	private StateMachineService stateMachineService;
	@Inject
	private BillRecalculationService billRecalculationService;

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/{id}")
	public Bill findById(@PathParam("id") String primaryKey) {
		return billService.findById(primaryKey);
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/find")
	public SearchResults<Bill> find(@QueryParam("n") Integer maxResults, @QueryParam("p") Integer page, @QueryParam("q") String queryString) {
		SearchParams params = new SearchParams();
		params.setItemsPerPage(maxResults);
		params.setCurrentPage(page);
		params.setQueryString(queryString);
		return billService.find(params);
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/detail/id/{id}")
	public BillDetail find(@PathParam("id") String id) {
		return entityManagerProvider.get().find(BillDetail.class, id);
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/merge")
	@Transactional
	public Message<Bill> merge(Bill bill) {
		try {
			EntityManager entityManager = entityManagerProvider.get();
			Bill current = entityManager.find(Bill.class, bill.getId());
			current.merge(bill);
			entityManager.merge(current);
			return new Message<>(Message.CODE_SUCCESS, i18nService.getMessage("bill.merge"), bill);
		} catch (Exception ex) {
			LOG.error("Error al actualizar la factura", ex);
			return new Message<>(Message.CODE_GENERIC_ERROR, i18nService.getMessage("bill.merge.error"));
		}
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/remove/{id}")
	@Transactional
	public Message<Bill> remove(@PathParam("id") String billId) {
		try {
			EntityManager entityManager = entityManagerProvider.get();
			Bill bill = entityManager.find(Bill.class, billId);
			billProcessor.remove(bill);
			return new Message<>(Message.CODE_SUCCESS, "Factura eliminada");
		} catch (Exception ex) {
			LOG.error("Error al eliminar la factura", ex);
			return new Message<>(Message.CODE_GENERIC_ERROR, "Error al eliminar la factura");
		}
	}

	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Path("confirm/{id}")
	@Transactional
	public Message<Bill> confirm(@PathParam("id") String id) {
		try {
			Bill bill = entityManagerProvider.get().find(Bill.class, id);
			billProcessor.confirmBill(bill);
			return new Message<>(Message.CODE_SUCCESS, i18nService.getMessage("bill.confirm.success"), bill);
		} catch (Exception ex) {
			LOG.error("Error al confirmar la factura", ex);
			return new Message<>(Message.CODE_GENERIC_ERROR, i18nService.getMessage("bill.confirm.error"));
		}
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("detail/liquidation/id/{id}")
	public BillLiquidationDetail mergeLiquidationDetail(@PathParam("id") String id) {
		return entityManagerProvider.get().find(BillLiquidationDetail.class, id);
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/detail/liquidation/merge")
	@Transactional
	public Message<Bill> mergeLiquidationDetail(BillLiquidationDetail detail) {
		try {
			Bill bill = billProcessor.mergeLiquidationDetail(detail);
			return new Message<>(Message.CODE_SUCCESS, i18nService.getMessage("bill.detail.merge"), bill);
		} catch (Exception ex) {
			LOG.error("Error al actualizar el detalle", ex);
			return new Message<>(Message.CODE_GENERIC_ERROR, i18nService.getMessage("bill.detail.merge.error"));
		}
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/detail/liquidation/remove/{id}")
	@Transactional
	public Message<Bill> removeLiquidationDetail(@PathParam("id") String id) {
		try {
			EntityManager entityManager = entityManagerProvider.get();
			BillLiquidationDetail detail = entityManager.find(BillLiquidationDetail.class, id);
			Bill bill = billProcessor.removeLiquidationDetail(detail);
			return new Message<>(Message.CODE_SUCCESS, i18nService.getMessage("bill.detail.remove"), bill);
		} catch (Exception ex) {
			LOG.error("Error al actualizar el detalle", ex);
			return new Message<>(Message.CODE_GENERIC_ERROR, i18nService.getMessage("bill.detail.remove.error"));
		}
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/detail/merge")
	@Transactional
	public Message<Bill> mergeBillDetail(BillDetail detail) {
		try {
			Bill bill = billProcessor.mergeBillDetail(detail);
			return new Message<>(Message.CODE_SUCCESS, i18nService.getMessage("bill.detail.merge"), bill);
		} catch (Exception ex) {
			LOG.error("Error al actualizar el detalle", ex);
			return new Message<>(Message.CODE_GENERIC_ERROR, i18nService.getMessage("bill.detail.merge.error"));
		}
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("/draft/{id}")
	@Transactional
	public Message<Bill> draft(@PathParam("id") String id) {
		try {
			EntityManager entityManager = entityManagerProvider.get();
			Bill bill = entityManager.find(Bill.class, id);
			stateMachineService.createTransition(bill, CommonState.Draft.name());
			return new Message<>(Message.CODE_SUCCESS, "Estado actualizado", bill);
		} catch (Exception ex) {
			LOG.error("Error al revertir el estado de la factura", ex);
			return new Message<>(Message.CODE_GENERIC_ERROR, "Error al revertir el estado de la factura");
		}
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("/detail/remove/{id}")
	@Transactional
	public Message<Bill> removeBillDetail(@PathParam("id") String id) {
		try {
			EntityManager entityManager = entityManagerProvider.get();
			BillDetail detail = entityManager.find(BillDetail.class, id);
			Bill bill = billProcessor.removeBillDetail(detail);
			return new Message<>(Message.CODE_SUCCESS, i18nService.getMessage("bill.detail.remove"), bill);
		} catch (Exception ex) {
			LOG.error("Error al eliminar el detalle", ex);
			return new Message<>(Message.CODE_GENERIC_ERROR, i18nService.getMessage("bill.detail.remove.error"), null);
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
	public Message<Bill> sendEmail(@PathParam("id") String id, String emailAddress) {
		try {
			if (StringUtils.isBlank(emailAddress)) {
				return new Message<>(Message.CODE_GENERIC_ERROR, "No se ha establecido el destinatario");
			} else {
				EntityManager entityManager = entityManagerProvider.get();
				entityManager.clear();
				Bill bill = entityManager.find(Bill.class, id);
				AppFile appFile = bill.getPdfFile();
				String title = "Factura " + bill.getCode();
				String body = "Adjunto PDF";
				String[] recipients = emailAddress.split("\\s*;\\s*");
				for (String recipient : recipients) {
					SendAppFileMailTask task = new SendAppFileMailTask(recipient, appFile, title, body, fileService, mailService);
					new Thread(task).start();
				}
				return new Message<>(Message.CODE_SUCCESS, String.format(i18nService.getMessage("bill.send.email"), emailAddress), bill);
			}
		} catch (Exception ex) {
			LOG.error("Error al enviar la factura", ex);
			return new Message<>(Message.CODE_SUCCESS, i18nService.getMessage("bill.send.email.error"));
		}
	}

	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/rectify/{id}")
	public Message<Bill> rectify(@PathParam("id") String id) {
		try {
			EntityManager entityManager = entityManagerProvider.get();
			entityManager.clear();
			Bill bill = entityManager.find(Bill.class, id);
			Bill rectified = billProcessor.rectifyBill(bill);
			return new Message<>(Message.CODE_SUCCESS, i18nService.getMessage("bill.rectify"), rectified);
		} catch (Exception ex) {
			LOG.error("Error al generar la rectificación", ex);
			return new Message<>(Message.CODE_GENERIC_ERROR, i18nService.getMessage("bill.rectify.error"), null);
		}
	}

	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/recalculate/{id}")
	public Message<Bill> recalculate(@PathParam("id") String billId) {
		return billRecalculationService.recalculate(billId);
	}

	@GET
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	@Path("/draft/{id}")
	public Response getArtifactBinaryContent(@PathParam("id") String id) {
		try {
			EntityManager entityManager = entityManagerProvider.get();
			Bill bill = entityManager.find(Bill.class, id);
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			pdfBillGenerator.generate(bill, out);
			ResponseBuilder response = Response.ok(new ByteArrayInputStream(out.toByteArray()));
			response.header("Content-Disposition", String.format("attachment; filename=\"%s\"", "borrador.pdf"));
			response.header("Content-Type", "application/pdf");
			return response.build();
		} catch (Exception ex) {
			LOG.error("Error al generar el borrador", ex);
			throw new RuntimeException("Error la generar el borrador");
		}
	}
}
