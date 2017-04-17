package com.luckia.biller.web.rest;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.annotation.security.PermitAll;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.mutable.Mutable;
import org.apache.commons.lang3.mutable.MutableObject;

import com.luckia.biller.core.common.BillerException;
import com.luckia.biller.core.common.NoAvailableDataException;
import com.luckia.biller.core.model.AppFile;
import com.luckia.biller.core.model.Company;
import com.luckia.biller.core.model.CompanyGroup;
import com.luckia.biller.core.model.CostCenter;
import com.luckia.biller.core.model.common.Message;
import com.luckia.biller.core.reporting.AdjustmentReportGenerator;
import com.luckia.biller.core.reporting.LiquidationReportGenerator;
import com.luckia.biller.core.reporting.LiquidationSummaryReportGenerator;
import com.luckia.biller.core.reporting.TerminalReportGenerator;
import com.luckia.biller.core.services.FileService;

import lombok.extern.slf4j.Slf4j;

/**
 * Servicio REST encargado de la generacion de informes.
 */
@Path("/report")
@Slf4j
public class ReportRestService extends AbstractBinaryRestService {

	private static final String DATE_FORMAT = "yyyy-MM-dd";
	private static final String CONTENT_DISPOSITION = "Content-Disposition";
	private static final String CONTENT_TYPE = "Content-Type";
	private static final String ATT_PATTERN = "attachment; filename=\"%s\"";

	@Inject
	private TerminalReportGenerator terminalReportGenerator;
	@Inject
	private LiquidationReportGenerator liquidationReportGenerator;
	@Inject
	private LiquidationSummaryReportGenerator liquidationSummaryReportGenerator;
	@Inject
	private AdjustmentReportGenerator adjustmentReportGenerator;
	@Inject
	private FileService fileService;

	/**
	 * Crea el informe de terminales a una fecha dada
	 * 
	 * @param dateAsString
	 * @param companyId
	 * @param costCenterId
	 * @return
	 */
	@GET
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	@Path("/terminals")
	@PermitAll
	public Response terminals(@QueryParam("date") String dateAsString,
			@QueryParam("companyId") Long companyId,
			@QueryParam("costCenterId") Long costCenterId) {
		try {
			Date date = Calendar.getInstance().getTime();
			EntityManager entityManager = entityManagerProvider.get();
			Company company = companyId != null
					? entityManager.find(Company.class, companyId) : null;
			Message<AppFile> message = terminalReportGenerator.generate(date, company);
			InputStream in = fileService.getInputStream(message.getPayload());
			ResponseBuilder response = Response.ok(in);
			response.header(CONTENT_DISPOSITION,
					String.format(ATT_PATTERN, "Terminales.xls"));
			response.header(CONTENT_TYPE, FileService.CONTENT_TYPE_EXCEL);
			return response.build();
		}
		catch (Exception ex) {
			log.error("Error al generar el informe de terminales", ex);
			throw new BillerException("Error la generar el informe de terminales");
		}
	}

	@GET
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	@Path("/liquidations")
	@PermitAll
	public Response liquidations(@QueryParam("from") String fromAsString,
			@QueryParam("to") String toAsString,
			@QueryParam("companyId") String companyId,
			@QueryParam("costCenterId") String costCenterId,
			@QueryParam("companyGroupId") String companyGroupId) {
		try {
			log.debug("Generando informe de liquidacines from {} to {}", fromAsString,
					toAsString);
			EntityManager entityManager = entityManagerProvider.get();
			Mutable<Date> from = new MutableObject<Date>();
			Mutable<Date> to = new MutableObject<Date>();
			Company company = StringUtils.isNotBlank(companyId)
					? entityManager.find(Company.class, Long.parseLong(companyId)) : null;
			CostCenter costCenter = StringUtils.isNotBlank(costCenterId)
					? entityManager.find(CostCenter.class, Long.parseLong(costCenterId))
					: null;
			CompanyGroup companyGroup = StringUtils.isNotBlank(companyGroupId)
					? entityManager.find(CompanyGroup.class,
							Long.parseLong(companyGroupId))
					: null;
			calculateRange(fromAsString, toAsString, from, to);
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			liquidationReportGenerator.generate(from.getValue(), to.getValue(), company,
					companyGroup, costCenter, out);
			ResponseBuilder response = Response
					.ok(new ByteArrayInputStream(out.toByteArray()));
			response.header(CONTENT_DISPOSITION,
					String.format(ATT_PATTERN, "Liquidaciones.xls"));
			response.header(CONTENT_TYPE, FileService.CONTENT_TYPE_EXCEL);
			return response.build();
		}
		catch (Exception ex) {
			log.error("Error al generar el informe de liquidaciones", ex);
			throw new BillerException("Error la generar el informe de liquidaciones");
		}
	}

	@GET
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	@Path("/liquidation-summary")
	@PermitAll
	public Response liquidationSummary(@QueryParam("from") String fromAsString,
			@QueryParam("to") String toAsString,
			@QueryParam("companyId") String companyId,
			@QueryParam("costCenterId") String costCenterId,
			@QueryParam("companyGroupId") String companyGroupId,
			@QueryParam("s") String sessionId) {
		if (!checkSessionId(sessionId)) {

		}
		try {
			log.debug("Generating liquidation summary report ({},{},{},{})", fromAsString,
					toAsString, companyId, costCenterId);
			Mutable<Date> from = new MutableObject<Date>();
			Mutable<Date> to = new MutableObject<Date>();
			calculateRange(fromAsString, toAsString, from, to);
			EntityManager entityManager = entityManagerProvider.get();
			Company company = StringUtils.isNotBlank(companyId)
					? entityManager.find(Company.class, Long.parseLong(companyId)) : null;
			CostCenter costCenter = StringUtils.isNotBlank(costCenterId)
					? entityManager.find(CostCenter.class, Long.parseLong(costCenterId))
					: null;
			CompanyGroup companyGroup = StringUtils.isNotBlank(companyGroupId)
					? entityManager.find(CompanyGroup.class,
							Long.parseLong(companyGroupId))
					: null;
			Message<AppFile> message = liquidationSummaryReportGenerator.generate(
					from.getValue(), to.getValue(), company, costCenter, companyGroup);
			InputStream in = fileService.getInputStream(message.getPayload());
			ResponseBuilder response = Response.ok(in);
			response.header(CONTENT_DISPOSITION,
					String.format(ATT_PATTERN, "Resumen de liquidaciones.xls"));
			response.header(CONTENT_TYPE, FileService.CONTENT_TYPE_EXCEL);
			return response.build();
		}
		catch (NoAvailableDataException ex) {
			log.trace("No data", ex);
			return sendRedirect(REDIRECT_NO_CONTENT_URI);
		}
		catch (Exception ex) {
			throw new BillerException("Liquidation report summary generation error", ex);
		}
	}

	@GET
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	@Path("/adjustments")
	@PermitAll
	public Response adjustments(@QueryParam("from") String fromAsString,
			@QueryParam("to") String toAsString,
			@QueryParam("companyId") String companyId,
			@QueryParam("costCenterId") String costCenterId,
			@QueryParam("companyGroupId") String companyGroupId,
			@QueryParam("sessionid") String sessionId) {
		try {
			log.debug("Generating liquidation summary report ({},{},{},{})", fromAsString,
					toAsString, companyId, costCenterId);
			EntityManager entityManager = entityManagerProvider.get();
			Mutable<Date> from = new MutableObject<Date>();
			Mutable<Date> to = new MutableObject<Date>();
			calculateRange(fromAsString, toAsString, from, to);
			Company company = StringUtils.isNotBlank(companyId)
					? entityManager.find(Company.class, Long.parseLong(companyId)) : null;
			CostCenter costCenter = StringUtils.isNotBlank(costCenterId)
					? entityManager.find(CostCenter.class, Long.parseLong(costCenterId))
					: null;
			CompanyGroup companyGroup = StringUtils.isNotBlank(companyGroupId)
					? entityManager.find(CompanyGroup.class,
							Long.parseLong(companyGroupId))
					: null;
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			adjustmentReportGenerator.generate(from.getValue(), to.getValue(), company,
					companyGroup, costCenter, out);
			InputStream in = new ByteArrayInputStream(out.toByteArray());
			ResponseBuilder response = Response.ok(in);
			response.header("Content-Disposition",
					String.format("attachment; filename=\"%s\"", "Ajustes manuales.xls"));
			response.header("Content-Type", FileService.CONTENT_TYPE_EXCEL);
			return response.build();
		}
		catch (NoAvailableDataException ex) {
			log.trace("No data", ex);
			return sendRedirect(REDIRECT_NO_CONTENT_URI);
		}
		catch (Exception ex) {
			throw new BillerException("Liquidation report summary generation error", ex);
		}
	}

	private void calculateRange(String fromAsString, String toAsString,
			Mutable<Date> resultFrom, Mutable<Date> resultTo) {
		try {
			if (StringUtils.isNotBlank(fromAsString)) {
				resultFrom
						.setValue(new SimpleDateFormat(DATE_FORMAT).parse(fromAsString));
			}
			if (StringUtils.isNotBlank(toAsString)) {
				resultTo.setValue(new SimpleDateFormat(DATE_FORMAT).parse(toAsString));
			}
			return;
		}
		catch (Exception ignore) {
			log.warn("Error setting report date range", ignore);
		}
	}
}
