package com.luckia.biller.core.reporting;

import java.io.OutputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.luckia.biller.core.model.Address;
import com.luckia.biller.core.model.Store;
import com.luckia.biller.core.model.TerminalRelation;
import com.luckia.biller.core.model.common.Message;

public class TerminalReportGenerator extends BaseReport {

	private static final Logger LOG = LoggerFactory.getLogger(TerminalReportGeneratorTest.class);

	@Inject
	private Provider<EntityManager> entityManagerProvider;

	public Message<String> generate(Date date, OutputStream out) {
		try {
			date = date == null ? Calendar.getInstance().getTime() : date;
			EntityManager entityManager = entityManagerProvider.get();
			TypedQuery<TerminalRelation> query = entityManager.createQuery("select e from TerminalRelation e order by e.code", TerminalRelation.class);
			List<TerminalRelation> relations = query.getResultList();
			HSSFWorkbook workbook = new HSSFWorkbook();
			HSSFSheet sheet = workbook.createSheet("Terminales");
			int rowIndex = 0;
			int cellIndex = 0;
			createHeaderCell(sheet, rowIndex, cellIndex++, "Terminal");
			createHeaderCell(sheet, rowIndex, cellIndex++, "Fecha");
			createHeaderCell(sheet, rowIndex, cellIndex++, "Establecimiento");
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
				createCell(sheet, rowIndex, cellIndex++, relation.getCode());
				createCell(sheet, rowIndex, cellIndex++, date);
				if (store != null) {
					Address address = relation.getStore().getAddress();
					createCell(sheet, rowIndex, cellIndex++, store.getName());
					createCell(sheet, rowIndex, cellIndex++, address != null && address.getProvince() != null ? address.getProvince().getName() : StringUtils.EMPTY);
					createCell(sheet, rowIndex, cellIndex++, address != null && address.getRegion() != null ? address.getRegion().getName() : StringUtils.EMPTY);
					createCell(sheet, rowIndex, cellIndex++, address != null && address.getZipCode() != null ? address.getZipCode() : StringUtils.EMPTY);
					createCell(sheet, rowIndex, cellIndex++, address != null && address.getRoad() != null ? address.getRoad() : StringUtils.EMPTY);
					createCell(sheet, rowIndex, cellIndex++, relation.getStore().getPhoneNumber() != null ? relation.getStore().getPhoneNumber() : StringUtils.EMPTY);
					createCell(sheet, rowIndex, cellIndex++, relation.getComments() != null ? relation.getComments() : StringUtils.EMPTY);
				}
			}
			for (int i = 0; i < 9; i++) {
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
