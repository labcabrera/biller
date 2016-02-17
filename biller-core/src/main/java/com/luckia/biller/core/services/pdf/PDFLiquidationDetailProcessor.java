package com.luckia.biller.core.services.pdf;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.luckia.biller.core.common.MathUtils;
import com.luckia.biller.core.model.Bill;
import com.luckia.biller.core.model.BillLiquidationDetail;
import com.luckia.biller.core.model.Liquidation;
import com.luckia.biller.core.model.LiquidationDetail;

/**
 * Servicio que realiza el desglose de la liquidacion en los siguientes conceptos:
 * <ul>
 * <li>Honorarios por apuestas (GGR / NGR / NR)</li>
 * <li>Ventas</li>
 * <li>SAT</li>
 * <li>Coste por ubicación</li>
 * <li>Atención comercial</li>
 * <li>Ajustes manuales</li>
 * </ul>
 */
public class PDFLiquidationDetailProcessor {

	private static final String HONORARIOS_APUESTAS = "Honorarios por apuestas";
	private static final String VENTAS = "Ventas";
	private static final String SAT = "SAT";
	private static final String COSTE_UBICACION = "Coste por ubicación";
	private static final String ATENCION_COMERCIAL = "Atención comercial";

	public Map<String, PDFLiquidationDetail> loadDetails(Liquidation liquidation) {
		Map<String, PDFLiquidationDetail> result = new LinkedHashMap<>();
		result.put(HONORARIOS_APUESTAS, new PDFLiquidationDetail().init("Honorarios por apuestas"));
		result.put(VENTAS, new PDFLiquidationDetail().init("Ventas"));
		result.put(SAT, new PDFLiquidationDetail().init("SAT"));
		result.put(COSTE_UBICACION, new PDFLiquidationDetail().init("Coste por ubicación"));
		result.put(ATENCION_COMERCIAL, new PDFLiquidationDetail().init("Atención comercial"));
		for (Bill bill : liquidation.getBills()) {
			for (BillLiquidationDetail detail : bill.getLiquidationDetails()) {
				if (detail.getLiquidationIncluded()) {
					switch (detail.getConcept()) {
					case GGR:
					case NGR:
					case NR:
						addConcept(result, HONORARIOS_APUESTAS, detail);
						break;
					case STAKES:
						addConcept(result, VENTAS, detail);
						break;
					case SAT_MONTHLY_FEES:
						addConcept(result, SAT, detail);
						break;
					case PRICE_PER_LOCATION:
						addConcept(result, COSTE_UBICACION, detail);
						break;
					case COMMERCIAL_MONTHLY_FEES:
						addConcept(result, ATENCION_COMERCIAL, detail);
						break;
					case MANUAL:
						addConcept(result, detail.getName(), detail);
						break;
					default:
						throw new RuntimeException("Unexpected concept type: " + detail.getConcept());
					}
				}
			}

		}
		return cleanEmptyResults(result);
	}

	public List<PDFLiquidationDetail> loadOuterDetails(Liquidation liquidation) {
		List<PDFLiquidationDetail> result = new ArrayList<>();

		// A nivel de liquidacion
		for (LiquidationDetail detail : liquidation.getDetails()) {
			if (!detail.getLiquidationIncluded()) {
				PDFLiquidationDetail pdfDetail = new PDFLiquidationDetail();
				pdfDetail.setAmount(detail.getValue());
				pdfDetail.setName(detail.getName());
				result.add(pdfDetail);
			}
		}

		// A nivel de facturas
		for (Bill bill : liquidation.getBills()) {
			for (BillLiquidationDetail detail : bill.getLiquidationDetails()) {
				if (!detail.getLiquidationIncluded()) {
					switch (detail.getConcept()) {
					case MANUAL:
						PDFLiquidationDetail pdfDetail = new PDFLiquidationDetail();
						pdfDetail.setName(detail.getName());
						pdfDetail.setAmount(detail.getValue());
						result.add(pdfDetail);
						break;
					default:
						break;
					}
				}
			}
		}

		return result;
	}

	private void addConcept(Map<String, PDFLiquidationDetail> map, String concept, BillLiquidationDetail detail) {
		if (!map.containsKey(concept)) {
			map.put(concept, new PDFLiquidationDetail().init(concept));
		}
		PDFLiquidationDetail i = map.get(concept);
		i.setNetAmount(i.getNetAmount().add(MathUtils.safeNull(detail.getNetValue())));
		i.setVatAmount(i.getVatAmount().add(MathUtils.safeNull(detail.getVatValue())));
		i.setAmount(i.getAmount().add(MathUtils.safeNull(detail.getValue())));
	}

	private Map<String, PDFLiquidationDetail> cleanEmptyResults(Map<String, PDFLiquidationDetail> map) {
		for (Iterator<Entry<String, PDFLiquidationDetail>> iterator = map.entrySet().iterator(); iterator.hasNext();) {
			Entry<String, PDFLiquidationDetail> entry = iterator.next();
			if (MathUtils.isZero(entry.getValue().getAmount())) {
				iterator.remove();
			}
		}
		return map;
	}
}
