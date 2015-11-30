package com.luckia.biller.web.rest;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.List;

import javax.annotation.security.PermitAll;
import javax.inject.Provider;
import javax.persistence.EntityManager;
import javax.validation.ValidationException;
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
import com.google.inject.persist.Transactional;
import com.luckia.biller.core.common.RegisterActivity;
import com.luckia.biller.core.i18n.I18nService;
import com.luckia.biller.core.model.AppFile;
import com.luckia.biller.core.model.Bill;
import com.luckia.biller.core.model.CommonState;
import com.luckia.biller.core.model.Liquidation;
import com.luckia.biller.core.model.LiquidationDetail;
import com.luckia.biller.core.model.UserActivityType;
import com.luckia.biller.core.model.common.Message;
import com.luckia.biller.core.model.common.SearchParams;
import com.luckia.biller.core.model.common.SearchResults;
import com.luckia.biller.core.services.FileService;
import com.luckia.biller.core.services.LiquidationMailService;
import com.luckia.biller.core.services.StateMachineService;
import com.luckia.biller.core.services.bills.BillProcessor;
import com.luckia.biller.core.services.bills.LiquidationProcessor;
import com.luckia.biller.core.services.entities.LiquidationEntityService;
import com.luckia.biller.core.services.pdf.PDFLiquidationGenerator;

/**
 * Servio REST asociado a las liquidaciones.
 */
@Path("/liquidations")
public class LiquidationRestService {

	private static final Logger LOG = LoggerFactory.getLogger(LiquidationRestService.class);

	@Inject
	private LiquidationEntityService entityService;
	@Inject
	private LiquidationProcessor liquidationProcessor;
	@Inject
	private Provider<EntityManager> entityManagerProvider;
	@Inject
	private LiquidationMailService liquidationMailService;
	@Inject
	private I18nService i18nService;
	@Inject
	private PDFLiquidationGenerator pdfLiquidationGenerator;
	@Inject
	private FileService fileService;
	@Inject
	private StateMachineService stateMachineService;
	@Inject
	private BillProcessor billProcessor;

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/id/{id}")
	public Liquidation findById(@PathParam("id") String primaryKey) {
		return entityService.findById(primaryKey);
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/find")
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
	@Transactional
	public Message<Liquidation> merge(Liquidation bill) {
		try {
			EntityManager entityManager = entityManagerProvider.get();
			Liquidation current = entityManager.find(Liquidation.class, bill.getId());
			current.merge(bill);
			entityManager.merge(current);
			return new Message<>(Message.CODE_SUCCESS, "Liquidación actualizada", bill);
		} catch (Exception ex) {
			LOG.error("Error al actualizar la factura", ex);
			return new Message<>(Message.CODE_GENERIC_ERROR, "Error al actualizar la liquidación");
		}
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/remove/{id}")
	public Message<Liquidation> remove(@PathParam("id") String liquidationId) {
		try {
			EntityManager entityManager = entityManagerProvider.get();
			Liquidation liquidation = entityManager.find(Liquidation.class, liquidationId);
			liquidationProcessor.remove(liquidation);
			return new Message<>(Message.CODE_SUCCESS, "Liquidación eliminada");
		} catch (Exception ex) {
			LOG.error("Error al eliminar la liquidacion", ex);
			return new Message<>(Message.CODE_GENERIC_ERROR, "Error al eliminar la liquidación");
		}
	}

	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Path("confirm/{id}")
	public Message<Liquidation> confirm(@PathParam("id") String id) {
		try {
			EntityManager entityManager = entityManagerProvider.get();
			entityManager.getEntityManagerFactory().getCache().evictAll();
			Liquidation liquidation = entityManager.find(Liquidation.class, id);
			liquidationProcessor.confirm(liquidation);
			return new Message<>(Message.CODE_SUCCESS, i18nService.getMessage("liquidation.confirm.success"), liquidation);
		} catch (ValidationException ex) {
			return new Message<>(Message.CODE_GENERIC_ERROR, ex.getMessage());
		} catch (Exception ex) {
			LOG.error("Error al confirmar la liquidacion", ex);
			return new Message<>(Message.CODE_GENERIC_ERROR, i18nService.getMessage("liquidation.confirm.error"));
		}
	}

	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Path("bills/confirm/{id}")
	public Message<List<Bill>> confirmAllPendingBills(@PathParam("id") String id) {
		try {
			EntityManager entityManager = entityManagerProvider.get();
			entityManager.getEntityManagerFactory().getCache().evictAll();
			Liquidation liquidation = entityManager.find(Liquidation.class, id);
			for (Bill bill : liquidation.getBills()) {
				if (CommonState.Draft.name().equals(bill.getCurrentState().getStateDefinition().getId())) {
					billProcessor.confirmBill(bill);
				}
			}
			return new Message<>(Message.CODE_SUCCESS, i18nService.getMessage("liquidation.confirm.pending.bills.success"), liquidation.getBills());
		} catch (ValidationException ex) {
			return new Message<>(Message.CODE_GENERIC_ERROR, ex.getMessage());
		} catch (Exception ex) {
			LOG.error("Error al confirmar la liquidacion", ex);
			return new Message<>(Message.CODE_GENERIC_ERROR, i18nService.getMessage("liquidation.confirm.error"));
		}
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/detail/id/{id}")
	public LiquidationDetail mergeDetail(@PathParam("id") String id) {
		return entityManagerProvider.get().find(LiquidationDetail.class, id);
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/detail/merge")
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
	@PermitAll
	public Response getArtifactBinaryContent(@PathParam("id") String id) {
		try {
			EntityManager entityManager = entityManagerProvider.get();
			entityManager.getEntityManagerFactory().getCache().evictAll();
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
	@RegisterActivity(type=UserActivityType.SEND_MAIL_LIQUIDATION)
	public Message<Liquidation> sendEmail(@PathParam("id") String id, String emailAddress) {
		try {
			EntityManager entityManager = entityManagerProvider.get();
			entityManager.clear();
			Liquidation liquidation = entityManager.find(Liquidation.class, id);
			String[] recipients = emailAddress.split("\\s*;\\s*");
			for (String recipient : recipients) {
				liquidationMailService.sendEmail(liquidation, recipient, false);
			}
			return new Message<>(Message.CODE_SUCCESS, String.format(i18nService.getMessage("liquidation.send.email.success"), emailAddress), liquidation);
		} catch (Exception ex) {
			LOG.error("Error al enviar la factura", ex);
			return new Message<>(Message.CODE_SUCCESS, i18nService.getMessage("liquidation.send.email.error"));
		}
	}

	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/pdf/recreate/{id}")
	@Transactional
	public Message<Liquidation> recreatePdf(@PathParam("id") String id) {
		try {
			EntityManager entityManager = entityManagerProvider.get();
			Liquidation liquidation = entityManager.find(Liquidation.class, id);
			File tempFile = File.createTempFile("tmp-bill-", ".pdf");
			FileOutputStream out = new FileOutputStream(tempFile);
			pdfLiquidationGenerator.generate(liquidation, out);
			out.close();
			FileInputStream in = new FileInputStream(tempFile);
			String name = String.format("bill-%s.pdf", liquidation.getId());
			AppFile pdfFile = fileService.save(name, "application/pdf", in);
			liquidation.setPdfFile(pdfFile);
			return new Message<>(Message.CODE_SUCCESS, "Se ha recreado el PDF de la liquidación", liquidation);
		} catch (Exception ex) {
			LOG.error("Error al recalcular la factura", ex);
			return new Message<>(Message.CODE_GENERIC_ERROR, "Error al recrear el PDF de la liquidación", null);
		}
	}

	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/recalculate/{id}")
	public Message<Liquidation> recalculate(@PathParam("id") String liquidationId) {
		try {
			Liquidation liquidation = liquidationProcessor.recalculate(liquidationId);
			return new Message<>(Message.CODE_SUCCESS, i18nService.getMessage("liquidation.recalculate"), liquidation);
		} catch (Exception ex) {
			LOG.error("Error al recalcular la factura", ex);
			return new Message<>(Message.CODE_GENERIC_ERROR, i18nService.getMessage("liquidation.recalculate.error"), null);
		}
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("/draft/{id}")
	@Transactional
	public Message<Liquidation> draft(@PathParam("id") String id) {
		try {
			EntityManager entityManager = entityManagerProvider.get();
			Liquidation liquidation = entityManager.find(Liquidation.class, id);
			stateMachineService.createTransition(liquidation, CommonState.Draft.name());
			return new Message<>(Message.CODE_SUCCESS, "Estado actualizado", liquidation);
		} catch (Exception ex) {
			LOG.error("Error al revertir el estado de la liquidacion", ex);
			return new Message<>(Message.CODE_GENERIC_ERROR, "Error al revertir el estado de la liquidacion");
		}
	}
}
