package com.luckia.biller.core.services.pdf;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.luckia.biller.core.common.MathUtils;
import com.luckia.biller.core.model.Bill;
import com.luckia.biller.core.model.BillConcept;
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

	public Map<BillConcept, PDFLiquidationDetail> loadDetails(Liquidation liquidation) {
		Map<BillConcept, PDFLiquidationDetail> result = new LinkedHashMap<>();
		result.put(BillConcept.BETTING_FEES, new PDFLiquidationDetail().init("Honorarios por apuestas"));
		result.put(BillConcept.STAKES, new PDFLiquidationDetail().init("Ventas"));
		result.put(BillConcept.SAT_MONTHLY_FEES, new PDFLiquidationDetail().init("SAT"));
		result.put(BillConcept.PRICE_PER_LOCATION, new PDFLiquidationDetail().init("Coste por ubicación"));
		result.put(BillConcept.COMMERCIAL_MONTHLY_FEES, new PDFLiquidationDetail().init("Atención comercial"));
		result.put(BillConcept.RAPPEL, new PDFLiquidationDetail().init(BillConcept.RAPPEL.description()));
		result.put(BillConcept.LOAN_RECOVERY, new PDFLiquidationDetail().init(BillConcept.LOAN_RECOVERY.description()));
		result.put(BillConcept.ROBBERY, new PDFLiquidationDetail().init(BillConcept.ROBBERY.description()));
		result.put(BillConcept.MANUAL, new PDFLiquidationDetail().init("Ajustes manuales de establecimientos"));
		for (Bill bill : liquidation.getBills()) {
			for (BillLiquidationDetail detail : bill.getLiquidationDetails()) {
				if (detail.getLiquidationIncluded()) {
					switch (detail.getConcept()) {
					case GGR:
					case NGR:
					case NR:
						addConcept(result, BillConcept.BETTING_FEES, detail);
						break;
					case STAKES:
						addConcept(result, BillConcept.STAKES, detail);
						break;
					case SAT_MONTHLY_FEES:
						addConcept(result, BillConcept.SAT_MONTHLY_FEES, detail);
						break;
					case PRICE_PER_LOCATION:
						addConcept(result, BillConcept.PRICE_PER_LOCATION, detail);
						break;
					case COMMERCIAL_MONTHLY_FEES:
						addConcept(result, BillConcept.COMMERCIAL_MONTHLY_FEES, detail);
						break;
					case MANUAL:
						BillConcept targetConcept = BillConcept.MANUAL;
						for (BillConcept i : BillConcept.values()) {
							if (result.containsKey(i) && i.description().equals(detail.getName())) {
								targetConcept = i;
								break;
							}
						}
						addConcept(result, targetConcept, detail);
						break;
					default:
						throw new RuntimeException("Unexpected concept type: " + detail.getConcept());
					}
				}
			}

		}
		return cleanEmptyResults(result);
	}

	public Map<BillConcept, PDFLiquidationDetail> loadOuterDetails(Liquidation liquidation) {
		Map<BillConcept, PDFLiquidationDetail> result = new LinkedHashMap<>();
		result.put(BillConcept.RAPPEL, new PDFLiquidationDetail().init("Rappel"));
		result.put(BillConcept.LOAN_RECOVERY, new PDFLiquidationDetail().init("Rappel"));
		result.put(BillConcept.ROBBERY, new PDFLiquidationDetail().init("Rappel"));
		result.put(BillConcept.MANUAL, new PDFLiquidationDetail().init("Ajustes manuales de establecimientos"));
		for (Bill bill : liquidation.getBills()) {
			for (BillLiquidationDetail detail : bill.getLiquidationDetails()) {
				if (!detail.getLiquidationIncluded()) {
					switch (detail.getConcept()) {
					case MANUAL:
						BillConcept targetConcept = BillConcept.MANUAL;
						for (BillConcept i : BillConcept.values()) {
							if (result.containsKey(i) && i.description().equals(detail.getName())) {
								targetConcept = i;
								break;
							}
						}
						addConcept(result, targetConcept, detail);
						break;
					default:
						throw new RuntimeException("Unexpected concept type: " + detail.getConcept());
					}
				}
			}
		}
		for (LiquidationDetail detail : liquidation.getDetails()) {
			if (!detail.getLiquidationIncluded()) {
				BillConcept targetConcept = BillConcept.MANUAL;
				for (BillConcept i : BillConcept.values()) {
					if (result.containsKey(i) && i.description().equals(detail.getName())) {
						targetConcept = i;
						break;
					}
				}
				addConcept(result, targetConcept, detail);
			}
		}
		return cleanEmptyResults(result);
	}

	private void addConcept(Map<BillConcept, PDFLiquidationDetail> map, BillConcept concept, BillLiquidationDetail detail) {
		PDFLiquidationDetail i = map.get(concept);
		i.setNetAmount(i.getNetAmount().add(MathUtils.safeNull(detail.getNetValue())));
		i.setVatAmount(i.getVatAmount().add(MathUtils.safeNull(detail.getVatValue())));
		i.setAmount(i.getAmount().add(MathUtils.safeNull(detail.getValue())));
	}

	private void addConcept(Map<BillConcept, PDFLiquidationDetail> map, BillConcept concept, LiquidationDetail detail) {
		PDFLiquidationDetail i = map.get(concept);
		i.setAmount(i.getAmount().add(MathUtils.safeNull(detail.getValue())));
	}

	private Map<BillConcept, PDFLiquidationDetail> cleanEmptyResults(Map<BillConcept, PDFLiquidationDetail> map) {
		for (Iterator<Entry<BillConcept, PDFLiquidationDetail>> iterator = map.entrySet().iterator(); iterator.hasNext();) {
			Entry<BillConcept, PDFLiquidationDetail> entry = iterator.next();
			if (MathUtils.isZero(entry.getValue().getAmount())) {
				iterator.remove();
			}
		}
		return map;
	}
}
