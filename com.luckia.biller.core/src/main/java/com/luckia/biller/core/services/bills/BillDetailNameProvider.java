package com.luckia.biller.core.services.bills;

import javax.inject.Inject;

import com.luckia.biller.core.common.MathUtils;
import com.luckia.biller.core.i18n.I18nService;
import com.luckia.biller.core.model.BillDetail;

/**
 * Componente encargado de establecer el literal de los detalles de una factura.
 */
public class BillDetailNameProvider {

	@Inject
	private I18nService i18nService;

	public String getName(BillDetail detail) {
		String conceptName = i18nService.getMessage("bill.concept.name." + detail.getConcept().name());
		StringBuffer sb = new StringBuffer(conceptName);
		if (MathUtils.isNotZeroPositive(detail.getBaseValue()) && MathUtils.isNotZeroPositive(detail.getPercent())) {
			sb.append(" (").append(detail.getPercent()).append("% sobre un total de ").append(detail.getBaseValue()).append(" €)");
		}
		return sb.toString();
	}

}
