package com.luckia.biller.web.rest;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.luckia.biller.core.model.AppFile;
import com.luckia.biller.core.model.LegalEntity;
import com.luckia.biller.core.model.common.Message;
import com.luckia.biller.core.reporting.LiquidationReportGenerator;
import com.luckia.biller.core.reporting.TerminalReportGenerator;
import com.luckia.biller.core.services.FileService;

@Path("/report")
public class ReportRestService {

	private static final Logger LOG = LoggerFactory.getLogger(ReportRestService.class);

	@Inject
	private TerminalReportGenerator terminalReportGenerator;
	@Inject
	private LiquidationReportGenerator liquidationReportGenerator;
	@Inject
	private FileService fileService;

	@GET
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	@Path("/terminals")
	public Response terminals(@QueryParam("date") String dateAsString) {
		try {
			Date date = Calendar.getInstance().getTime();
			Message<AppFile> message = terminalReportGenerator.generate(date);
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
	public Response liquidations(@QueryParam("from") String fromAsString, @QueryParam("to") String toAsString) {
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			Date from = new SimpleDateFormat("").parse(fromAsString);
			Date to = new SimpleDateFormat("").parse(toAsString);
			List<LegalEntity> entities = new ArrayList<LegalEntity>();
			liquidationReportGenerator.generate(from, to, entities, out);
			ResponseBuilder response = Response.ok(new ByteArrayInputStream(out.toByteArray()));
			response.header("Content-Disposition", String.format("attachment; filename=\"%s\"", "Liquidaciones.xls"));
			response.header("Content-Type", FileService.CONTENT_TYPE_EXCEL);
			return response.build();
		} catch (Exception ex) {
			LOG.error("Error al generar el informe de liquidaciones", ex);
			throw new RuntimeException("Error la generar el informe de liquidaciones");
		}
	}
}
