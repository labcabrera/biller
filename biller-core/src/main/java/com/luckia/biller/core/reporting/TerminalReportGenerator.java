package com.luckia.biller.core.reporting;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.luckia.biller.core.model.Address;
import com.luckia.biller.core.model.AppFile;
import com.luckia.biller.core.model.Company;
import com.luckia.biller.core.model.CostCenter;
import com.luckia.biller.core.model.Store;
import com.luckia.biller.core.model.TerminalRelation;
import com.luckia.biller.core.model.common.Message;
import com.luckia.biller.core.services.FileService;

/**
 * Componente encargado de generar una hoja de calculo con el informe de terminales registrados en la aplicacion.
 */
public class TerminalReportGenerator extends BaseReport {

	private static final Logger LOG = LoggerFactory.getLogger(TerminalReportGenerator.class);

	@Inject
	private Provider<EntityManager> entityManagerProvider;
	@Inject
	private FileService fileService;

	public Message<AppFile> generate(Date date, Company company, CostCenter costCenter) {
		date = date != null ? date : Calendar.getInstance().getTime();
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		generate(date, company, costCenter, out);
		ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
		String fileName = String.format("Terminales-%s.xls", DateFormatUtils.ISO_DATE_FORMAT.format(date));
		AppFile appFile = fileService.save(fileName, FileService.CONTENT_TYPE_EXCEL, in);
		return new Message<AppFile>(Message.CODE_SUCCESS, "Informe generado", appFile);
	}

	public Message<String> generate(Date date, Company company, CostCenter costCenter, OutputStream out) {
		try {
			Validate.notNull(date);
			LOG.debug("Generando informe de terminales a fecha {}", DateFormatUtils.ISO_DATE_FORMAT.format(date));
			EntityManager entityManager = entityManagerProvider.get();
			TypedQuery<TerminalRelation> query;
			if (company != null) {
				query = entityManager.createQuery("select e from TerminalRelation e where e.store.parent = :company order by e.code", TerminalRelation.class);
				query.setParameter("company", company);
			} else {
				query = entityManager.createQuery("select e from TerminalRelation e order by e.code", TerminalRelation.class);
			}
			List<TerminalRelation> relations = query.getResultList();
			LOG.debug("Encontrados {} terminales", relations.size());
			HSSFWorkbook workbook = new HSSFWorkbook();
			init(workbook);
			HSSFSheet sheet = workbook.createSheet("Terminales " + new SimpleDateFormat("dd-MM-yyyy").format(date));
			int rowIndex = 0;
			int cellIndex = 0;
			createHeaderCell(sheet, rowIndex, cellIndex++, "Terminal");
			createHeaderCell(sheet, rowIndex, cellIndex++, "Establecimiento");
			createHeaderCell(sheet, rowIndex, cellIndex++, "Operador");
			createHeaderCell(sheet, rowIndex, cellIndex++, "Centro de coste");
			createHeaderCell(sheet, rowIndex, cellIndex++, "Fecha inicio");
			createHeaderCell(sheet, rowIndex, cellIndex++, "Fecha fin");
			createHeaderCell(sheet, rowIndex, cellIndex++, "Provincia");
			createHeaderCell(sheet, rowIndex, cellIndex++, "Localidad");
			createHeaderCell(sheet, rowIndex, cellIndex++, "Código postal");
			createHeaderCell(sheet, rowIndex, cellIndex++, "Dirección");
			createHeaderCell(sheet, rowIndex, cellIndex++, "Teléfono");
			createHeaderCell(sheet, rowIndex, cellIndex++, "Comentarios");
			for (TerminalRelation relation : relations) {
				Store store = relation.getStore();
				cellIndex = 0;
				rowIndex++;
				if (store != null) {
					Address address = relation.getStore().getAddress();
					createCell(sheet, rowIndex, cellIndex++, relation.getCode());
					createCell(sheet, rowIndex, cellIndex++, store.getName());
					createCell(sheet, rowIndex, cellIndex++, store.getParent() != null ? store.getParent().getName() : StringUtils.EMPTY);
					createCell(sheet, rowIndex, cellIndex++, store.getCostCenter() != null ? store.getCostCenter().getName() : StringUtils.EMPTY);
					createCell(sheet, rowIndex, cellIndex++, store.getStartDate());
					createCell(sheet, rowIndex, cellIndex++, store.getEndDate());
					createCell(sheet, rowIndex, cellIndex++, address != null && address.getProvince() != null ? address.getProvince().getName() : StringUtils.EMPTY);
					createCell(sheet, rowIndex, cellIndex++, address != null && address.getRegion() != null ? address.getRegion().getName() : StringUtils.EMPTY);
					createCell(sheet, rowIndex, cellIndex++, address != null && address.getZipCode() != null ? address.getZipCode() : StringUtils.EMPTY);
					createCell(sheet, rowIndex, cellIndex++, address != null && address.getRoad() != null ? address.getRoad() : StringUtils.EMPTY);
					createCell(sheet, rowIndex, cellIndex++, relation.getStore().getPhoneNumber() != null ? relation.getStore().getPhoneNumber() : StringUtils.EMPTY);
					createCell(sheet, rowIndex, cellIndex++, store.getComments() != null ? store.getComments() : StringUtils.EMPTY);
				} else {
					createDisabledCell(sheet, rowIndex, cellIndex++, relation.getCode());
					for (int i = 0; i < 11; i++) {
						createDisabledCell(sheet, rowIndex, cellIndex++, StringUtils.EMPTY);
					}
				}
			}
			for (int i = 0; i < 20; i++) {
				sheet.autoSizeColumn(i);
			}
			workbook.write(out);
			out.flush();
			out.close();
			return new Message<>(Message.CODE_SUCCESS, String.format("Generado report de %s terminales", relations.size()));
		} catch (Exception ex) {
			LOG.error("Error al generar el report de liquidaciones", ex);
			return new Message<>(Message.CODE_GENERIC_ERROR, "Error al generar el report de liquidaciones");
		}
	}
}
