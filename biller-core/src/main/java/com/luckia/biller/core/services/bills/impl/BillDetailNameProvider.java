package com.luckia.biller.core.services.bills.impl;

import static com.luckia.biller.core.common.MathUtils.isNotZero;

import javax.inject.Inject;

import com.luckia.biller.core.i18n.I18nService;
import com.luckia.biller.core.model.AbstractBillDetail;
import com.luckia.biller.core.model.BillLiquidationDetail;

/**
 * Componente encargado de establecer el literal de los detalles de una factura.
 */
public class BillDetailNameProvider {

	@Inject
	private I18nService i18nService;

	public String getName(AbstractBillDetail detail) {
		String conceptName;
		if (BillLiquidationDetail.class.isAssignableFrom(detail.getClass())) {
			BillLiquidationDetail billLiquidationDetail = (BillLiquidationDetail) detail;
			conceptName = i18nService.getMessage(
					"bill.concept.name." + billLiquidationDetail.getConcept().name());
		}
		else {
			conceptName = i18nService
					.getMessage("bill.concept.name." + detail.getConcept().name());
		}
		StringBuilder sb = new StringBuilder(conceptName);
		if (isNotZero(detail.getSourceValue()) && isNotZero(detail.getPercent())) {
			sb.append(" (");
			sb.append(detail.getPercent());
			sb.append("% sobre un total de ");
			sb.append(detail.getSourceValue());
			sb.append(" €)");
		}
		return sb.toString();
	}
}
