package com.luckia.biller.core.services;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.inject.Inject;

import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.luckia.biller.core.common.MathUtils;
import com.luckia.biller.core.model.Bill;
import com.luckia.biller.core.model.Company;
import com.luckia.biller.core.model.Liquidation;
import com.luckia.biller.core.reporting.LiquidationReportGenerator;

/**
 * Genera un ZIP con los ficheros de la liquidacion mensual. A partir de una liquidacion genera un ZIP dentro del cual se encuentran todas las facturas asociadas a la liquidación.
 */
public class ZipFileService {

	private static final Logger LOG = LoggerFactory.getLogger(ZipFileService.class);

	private static final String FORMAT_BILL_FOLDER = "facturas/";

	@Inject
	private FileService fileService;
	@Inject
	private LiquidationReportGenerator reportGenerator;

	public void generate(Liquidation liquidation, OutputStream out) {
		Validate.notNull(liquidation.getPdfFile(), "Missing liquidation file");
		String name;
		InputStream in;
		try {
			ZipOutputStream zipOutputStream = new ZipOutputStream(out);
			name = fileService.getAbstractBillFileName(liquidation, "pdf");
			in = fileService.getInputStream(liquidation.getPdfFile());
			addZipEntry(in, zipOutputStream, name);
			for (Bill bill : liquidation.getBills()) {
				if (MathUtils.isNotZero(bill.getAmount()) && bill.getPdfFile() != null) {
					name = fileService.getAbstractBillFileName(bill, "pdf");
					in = fileService.getInputStream(bill.getPdfFile());
					addZipEntry(in, zipOutputStream, FORMAT_BILL_FOLDER + name);
				} else {
					LOG.debug("No se incluye la factura de {}: carece de fichero asociado", bill.getSender().getName());
				}
			}
			// Generamos el report
			try {
				Date from = liquidation.getDateFrom();
				Date to = liquidation.getDateTo();
				ByteArrayOutputStream reportOutputStream = new ByteArrayOutputStream();
				Company company = liquidation.getSender().as(Company.class);
				reportGenerator.generate(from, to, Arrays.asList(company), reportOutputStream);
				ByteArrayInputStream reportInputStream = new ByteArrayInputStream(reportOutputStream.toByteArray());
				String fileName = fileService.getAbstractBillFileName(liquidation, "xls");
				addZipEntry(reportInputStream, zipOutputStream, fileName);
			} catch (Exception ignore) {
				LOG.error("Error al adjuntar el report", ignore);
			}
			zipOutputStream.flush();
			zipOutputStream.close();
		} catch (Exception ex) {
			throw new RuntimeException("Error al generar el ZIP con la liquidación", ex);
		}
	}

	private void addZipEntry(InputStream inputStream, ZipOutputStream zipOutputStream, String name) throws IOException {
		ZipEntry zipEntry = new ZipEntry(name);
		byte[] buf = new byte[1024];
		int len;
		zipOutputStream.putNextEntry(zipEntry);
		while ((len = inputStream.read(buf)) > 0) {
			zipOutputStream.write(buf, 0, len);
		}
	}
}
