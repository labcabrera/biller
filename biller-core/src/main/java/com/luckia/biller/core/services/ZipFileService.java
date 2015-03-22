package com.luckia.biller.core.services;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.luckia.biller.core.model.Bill;
import com.luckia.biller.core.model.Liquidation;

/**
 * Genera un ZIP con los ficheros de la liquidacion mensual. A partir de una liquidacion genera un ZIP dentro del cual se encuentran todas
 * las facturas asociadas a la liquidación.
 */
public class ZipFileService {

	private static final Logger LOG = LoggerFactory.getLogger(ZipFileService.class);
	private static final String DATE_FORMAT = "yyyy-MM-dd";
	private static final String[] REPLACEMENTS_KEYS = { "á", "é", "í", "ó", "ú", "ñ", " ", "." };
	private static final String[] REPLACEMENTS_VALUES = { "a", "e", "i", "o", "u", "n", "_", "" };
	private static final String FORMAT_LIQUIDATION_NAME = "liquidacion-%s-%s.pdf";
	private static final String FORMAT_BILL_NAME = "factura-%s-%s.pdf";
	private static final String FORMAT_BILL_FOLDER = "facturas/";

	@Inject
	private FileService fileService;

	public void generate(Liquidation liquidation, OutputStream out) {
		Validate.notNull(liquidation.getPdfFile(), "Missing liquidation file");
		String name;
		String sender;
		InputStream in;
		try {
			ZipOutputStream zipOutputStream = new ZipOutputStream(out);
			sender = normalizeName(liquidation.getSender().getName());
			name = fileService.normalizeFileName(String.format(FORMAT_LIQUIDATION_NAME, sender, formatDate(liquidation.getBillDate())));
			in = fileService.getInputStream(liquidation.getPdfFile());
			addZipEntry(in, zipOutputStream, name);
			for (Bill bill : liquidation.getBills()) {
				if (bill.getPdfFile() != null) {
					sender = normalizeName(bill.getSender().getName());
					name = fileService.normalizeFileName(String.format(FORMAT_BILL_NAME, sender, formatDate(liquidation.getBillDate())));
					in = fileService.getInputStream(bill.getPdfFile());
					addZipEntry(in, zipOutputStream, FORMAT_BILL_FOLDER + name);
				} else {
					LOG.debug("No se incluye la factura de {}: carece de fichero asociado", bill.getSender().getName());
				}
			}
			zipOutputStream.flush();
			zipOutputStream.close();
		} catch (Exception ex) {
			throw new RuntimeException("Error al generar el ZIP con la liquidación", ex);
		}
	}

	private String normalizeName(String name) {
		name = name.toLowerCase();
		for (int i = 0; i < REPLACEMENTS_KEYS.length; i++) {
			name = name.replace(REPLACEMENTS_KEYS[i], REPLACEMENTS_VALUES[i]);
		}
		return name;
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

	private String formatDate(Date date) {
		return date != null ? new SimpleDateFormat(DATE_FORMAT).format(date) : StringUtils.EMPTY;
	}
}
