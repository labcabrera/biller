package com.luckia.biller.web.rest;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.annotation.security.PermitAll;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.luckia.biller.core.model.AppFile;
import com.luckia.biller.core.model.Company;
import com.luckia.biller.core.model.CompanyGroup;
import com.luckia.biller.core.model.CostCenter;
import com.luckia.biller.core.model.common.Message;
import com.luckia.biller.core.reporting.LiquidationReportGenerator;
import com.luckia.biller.core.reporting.LiquidationSummaryReportGenerator;
import com.luckia.biller.core.reporting.TerminalReportGenerator;
import com.luckia.biller.core.services.FileService;

/**
 * Servicio REST encargado de la generacion de informes
 */
@Path("/report")
public class ReportRestService {

	private static final Logger LOG = LoggerFactory.getLogger(ReportRestService.class);

	@Inject
	private TerminalReportGenerator terminalReportGenerator;
	@Inject
	private LiquidationReportGenerator liquidationReportGenerator;
	@Inject
	private LiquidationSummaryReportGenerator liquidationSummaryReportGenerator;
	@Inject
	private FileService fileService;
	@Inject
	private Provider<EntityManager> entityManagerProvider;

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
	public Response terminals(@QueryParam("date") String dateAsString, @QueryParam("companyId") Long companyId, @QueryParam("costCenterId") Long costCenterId) {
		try {
			Date date = Calendar.getInstance().getTime();
			EntityManager entityManager = entityManagerProvider.get();
			Company company = companyId != null ? entityManager.find(Company.class, companyId) : null;
			CostCenter costCenter = costCenterId != null ? entityManager.find(CostCenter.class, costCenterId) : null;
			Message<AppFile> message = terminalReportGenerator.generate(date, company, costCenter);
			InputStream in = fileService.getInputStream(message.getPayload());
			ResponseBuilder response = Response.ok(in);
			response.header("Content-Disposition", String.format("attachment; filename=\"%s\"", "Terminales.xls"));
			response.header("Content-Type", FileService.CONTENT_TYPE_EXCEL);
			return response.build();
		} catch (Exception ex) {
			LOG.error("Error al generar el informe de terminales", ex);
			throw new RuntimeException("Error la generar el informe de terminales");
		}
	}

	@GET
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	@Path("/liquidations")
	@PermitAll
	public Response liquidations(@QueryParam("from") String fromAsString, @QueryParam("to") String toAsString, @QueryParam("ids") String entityIds) {
		try {
			LOG.debug("Generando informe de liquidacines ({},{},{}", fromAsString, toAsString, entityIds);
			Mutable<Date> from = new MutableObject<Date>();
			Mutable<Date> to = new MutableObject<Date>();
			calculateRange(fromAsString, toAsString, from, to);
			String[] ids = entityIds.split("\\s,\\s");
			List<Company> entities;
			if (StringUtils.isNotBlank(entityIds) && ids.length > 0) {
				String qlString = "select e from LegalEntity e where e.id in :ids";
				TypedQuery<Company> query = entityManagerProvider.get().createQuery(qlString, Company.class);
				entities = query.setParameter("ids", Arrays.asList(ids)).getResultList();
			} else {
				entities = entityManagerProvider.get().createQuery("select e from Company e", Company.class).getResultList();
			}
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			liquidationReportGenerator.generate(from.getValue(), to.getValue(), entities, out);
			ResponseBuilder response = Response.ok(new ByteArrayInputStream(out.toByteArray()));
			response.header("Content-Disposition", String.format("attachment; filename=\"%s\"", "Liquidaciones.xls"));
			response.header("Content-Type", FileService.CONTENT_TYPE_EXCEL);
			return response.build();
		} catch (Exception ex) {
			LOG.error("Error al generar el informe de liquidaciones", ex);
			throw new RuntimeException("Error la generar el informe de liquidaciones");
		}
	}

	@GET
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	@Path("/liquidation-summary")
	@PermitAll
	public Response liquidationSummary(@QueryParam("from") String fromAsString, @QueryParam("to") String toAsString, @QueryParam("companyId") String companyId,
			@QueryParam("costCenterId") String costCenterId, @QueryParam("companyGroupId") String companyGroupId) {
		try {
			LOG.debug("Generating liquidation summary report ({},{},{},{})", fromAsString, toAsString, companyId, costCenterId);
			Mutable<Date> from = new MutableObject<Date>();
			Mutable<Date> to = new MutableObject<Date>();
			calculateRange(fromAsString, toAsString, from, to);
			EntityManager entityManager = entityManagerProvider.get();
			Company company = StringUtils.isNotBlank(companyId) ? entityManager.find(Company.class, Long.parseLong(companyId)) : null;
			CostCenter costCenter = StringUtils.isNotBlank(costCenterId) ? entityManager.find(CostCenter.class, Long.parseLong(costCenterId)) : null;
			CompanyGroup companyGroup = StringUtils.isNotBlank(companyGroupId) ? entityManager.find(CompanyGroup.class, Long.parseLong(companyGroupId)) : null;
			Message<AppFile> message = liquidationSummaryReportGenerator.generate(from.getValue(), to.getValue(), company, costCenter, companyGroup);
			InputStream in = fileService.getInputStream(message.getPayload());
			ResponseBuilder response = Response.ok(in);
			response.header("Content-Disposition", String.format("attachment; filename=\"%s\"", "Resumen de liquidaciones.xls"));
			response.header("Content-Type", FileService.CONTENT_TYPE_EXCEL);
			return response.build();
		} catch (Exception ex) {
			throw new RuntimeException("Error la generar el informe de liquidaciones", ex);
		}
	}

	private void calculateRange(String fromAsString, String toAsString, Mutable<Date> resultFrom, Mutable<Date> resultTo) {
		try {
			if (StringUtils.isNotBlank(fromAsString)) {
				resultFrom.setValue(new SimpleDateFormat("yyyy-MM-dd").parse(fromAsString));
			}
			if (StringUtils.isNotBlank(toAsString)) {
				resultTo.setValue(new SimpleDateFormat("yyyy-MM-dd").parse(toAsString));
			}
			return;
		} catch (Exception ignore) {
			LOG.warn("Error setting report date range", ignore);
		}
	}

}
