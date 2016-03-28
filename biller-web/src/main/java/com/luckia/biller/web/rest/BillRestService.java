package com.luckia.biller.web.rest;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import javax.annotation.security.PermitAll;
import javax.inject.Provider;
import javax.persistence.EntityManager;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import com.luckia.biller.core.common.RegisterActivity;
import com.luckia.biller.core.i18n.I18nService;
import com.luckia.biller.core.model.AppFile;
import com.luckia.biller.core.model.Bill;
import com.luckia.biller.core.model.BillDetail;
import com.luckia.biller.core.model.BillLiquidationDetail;
import com.luckia.biller.core.model.CommonState;
import com.luckia.biller.core.model.UserActivityType;
import com.luckia.biller.core.model.UserRole;
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
import com.luckia.biller.core.services.pdf.PDFLiquidationDetailGenerator;
import com.luckia.biller.core.services.security.RequiredRole;

/**
 * Common bill REST operations.
 */
@Path("/bills")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
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
	private PDFLiquidationDetailGenerator pdfLiquidationDetailGenerator;
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
	@Path("/{id}")
	public Bill findById(@PathParam("id") String primaryKey) {
		Bill result = billService.findById(primaryKey);
		if (result == null) {
			throw new WebApplicationException(HttpStatus.SC_NOT_FOUND);
		}
		return result;
	}

	@GET
	@Path("/find")
	public SearchResults<Bill> find(@QueryParam("n") Integer maxResults, @QueryParam("p") Integer page, @QueryParam("q") String queryString) {
		SearchParams params = new SearchParams();
		params.setItemsPerPage(maxResults);
		params.setCurrentPage(page);
		params.setQueryString(queryString);
		return billService.find(params);
	}

	@GET
	@Path("/detail/id/{id}")
	public BillDetail find(@PathParam("id") String id) {
		return entityManagerProvider.get().find(BillDetail.class, id);
	}

	@POST
	@Path("/merge")
	@Transactional
	@RegisterActivity(type = UserActivityType.BILL_MERGE)
	@RequiredRole(any = { UserRole.CODE_OPERATOR, UserRole.CODE_ADMIN })
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
	@Path("detail/liquidation/id/{id}")
	public BillLiquidationDetail findLiquidationDetail(@PathParam("id") String id) {
		return entityManagerProvider.get().find(BillLiquidationDetail.class, id);
	}

	@POST
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
	@Path("/draft/{id}")
	@Transactional
	public Message<Bill> draft(@PathParam("id") String id) {
		try {
			EntityManager entityManager = entityManagerProvider.get();
			Bill bill = entityManager.find(Bill.class, id);
			stateMachineService.createTransition(bill, CommonState.DRAFT.name());
			return new Message<>(Message.CODE_SUCCESS, "Estado actualizado", bill);
		} catch (Exception ex) {
			LOG.error("Error al revertir el estado de la factura", ex);
			return new Message<>(Message.CODE_GENERIC_ERROR, "Error al revertir el estado de la factura");
		}
	}

	@POST
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
	 * Envia el PDF de la liquidacion por correo a un destinatario.
	 * 
	 * @param id
	 * @param emailAddress
	 *            destinatarios de correo
	 * @return
	 */
	@POST
	@Path("/send/{option}/{id}")
	public Message<Bill> sendEmail(@PathParam("option") String option, @PathParam("id") String id, String emailAddress) {
		try {
			if (StringUtils.isBlank(emailAddress)) {
				return new Message<>(Message.CODE_GENERIC_ERROR, "No se ha establecido el destinatario");
			} else {
				EntityManager entityManager = entityManagerProvider.get();
				entityManager.clear();
				Bill bill = entityManager.find(Bill.class, id);
				AppFile appFile;
				switch (option != null ? option : StringUtils.EMPTY) {
				case "bill":
					appFile = bill.getPdfFile();
					break;
				default:
					appFile = bill.getLiquidationDetailFile();
					break;
				}
				String title = "Factura " + bill.getCode();
				String body = "Adjunto PDF";
				String[] recipients = emailAddress.split("\\s*;\\s*");
				// TODO cuando se produce un error no se detecta y se devuelve el message success
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
	@Path("/recalculate/{id}")
	@RequiredRole(any = { UserRole.CODE_OPERATOR, UserRole.CODE_ADMIN })
	public Message<Bill> recalculate(@PathParam("id") String billId) {
		return billRecalculationService.recalculate(billId);
	}

	@GET
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	@Path("/draft/bill/{id}")
	@PermitAll
	public Response generatePdfDraft(@PathParam("id") String id) {
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

	@GET
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	@Path("/draft/liquidation-detail/{id}")
	@PermitAll
	public Response generatePdfLiquidationDetailDraft(@PathParam("id") String id) {
		try {
			EntityManager entityManager = entityManagerProvider.get();
			Bill bill = entityManager.find(Bill.class, id);
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			pdfLiquidationDetailGenerator.generate(bill, out);
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
