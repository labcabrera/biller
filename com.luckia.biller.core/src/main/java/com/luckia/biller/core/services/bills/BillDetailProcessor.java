package com.luckia.biller.core.services.bills;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.inject.Inject;

import org.apache.commons.lang3.Range;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.luckia.biller.core.common.MathUtils;
import com.luckia.biller.core.i18n.I18nService;
import com.luckia.biller.core.model.Bill;
import com.luckia.biller.core.model.BillConcept;
import com.luckia.biller.core.model.BillDetail;
import com.luckia.biller.core.model.BillingModel;
import com.luckia.biller.core.model.LiquidationDetail;
import com.luckia.biller.core.model.Store;
import com.luckia.biller.core.model.TerminalRelation;

/**
 * Servicio encargado de crear los detalles de una factura a partir de los datos obtenidos a traves de {@link BillDataProvider}. Esto genera
 * dos listas de conceptos:
 * <ul>
 * <li>Conceptos de facturacion: en principio solo se facturara a los bares por un porcentaje de las ventas</li>
 * <li>Conceptos de liquidacion: en este punto se generan los conceptos a partir de los cuales se realizara la liquidacion conjunta.</li>
 * <li></li>
 * </ul>
 */
public class BillDetailProcessor {

	private static final Logger LOG = LoggerFactory.getLogger(BillDetailProcessor.class);

	@Inject
	private BillDataProvider billingDataProvider;
	@Inject
	private I18nService i18nService;
	@Inject
	private BillDetailNameProvider billDetailNameProvider;

	public void process(Bill bill) {
		Store store = bill.getSender(Store.class);
		BillingModel model = bill.getModel();
		List<String> terminals = resolveTerminals(store);
		if (terminals.isEmpty()) {
			LOG.info("No se puede generar la factura de {}. No tiene terminales asociados.", store.getName());
		} else {
			Range<Date> range = Range.between(bill.getDateFrom(), bill.getDateTo());
			Map<BillConcept, BigDecimal> billingData = billingDataProvider.retreive(bill, range, terminals);

			// Calculamos los conceptos de la facturacion. CUIDADO: los importes que provienen de LIS tienen IVA, de modo que para hacer el
			// calculo debemos volver a calcular la base
			if (MathUtils.isNotZero(model.getStakesPercentStore())) {
				BigDecimal percent = model.getStakesPercentStore();
				BigDecimal stakes = billingData.get(BillConcept.Stakes).divide(new BigDecimal("1.21"), 2, RoundingMode.HALF_EVEN);
				BigDecimal value = stakes.multiply(percent).divide(MathUtils.HUNDRED, 2, RoundingMode.HALF_EVEN);
				addBillingConcept(bill, BillConcept.Stakes, value, stakes, percent);
			}

			// Calculamos los conceptos de la liquidacion definidos a nivel de los porcentajes de las variables del terminal:
			Map<BillConcept, BigDecimal> percentConcepts = new LinkedHashMap<BillConcept, BigDecimal>();
			percentConcepts.put(BillConcept.NGR, model.getNgrPercent());
			percentConcepts.put(BillConcept.GGR, model.getGgrPercent());
			percentConcepts.put(BillConcept.NR, model.getNrPercent());
			percentConcepts.put(BillConcept.Stakes, model.getStakesPercentOperator());
			for (BillConcept concept : percentConcepts.keySet()) {
				BigDecimal percent = percentConcepts.get(concept);
				if (MathUtils.isNotZero(percent)) {
					addLiquidationConcept(bill, concept, percent, billingData);
				}
			}

			// Calculamos los conceptos fijos
			processLiquidationFixedConcepts(bill, model, range, terminals);
		}
	}

	private void processLiquidationFixedConcepts(Bill bill, BillingModel model, Range<Date> range, List<String> terminals) {
		Map<BillConcept, BigDecimal> fixedConcepts = new LinkedHashMap<BillConcept, BigDecimal>();
		fixedConcepts.put(BillConcept.CommercialMonthlyFees, model.getCommercialMonthlyFees());
		fixedConcepts.put(BillConcept.SatMonthlyFees, model.getSatMonthlyFees());
		for (BillConcept concept : fixedConcepts.keySet()) {
			BigDecimal value = fixedConcepts.get(concept);
			if (MathUtils.isNotZeroPositive(value)) {
				addLiquidationFixedConcept(bill, concept, value);
			}
		}
	}

	private void addBillingConcept(Bill bill, BillConcept concept, BigDecimal value, BigDecimal total, BigDecimal percent) {
		BillDetail detail = new BillDetail();
		detail.setId(UUID.randomUUID().toString());
		detail.setConcept(concept);
		detail.setValue(value);
		detail.setBill(bill);
		detail.setBaseValue(total);
		detail.setPercent(percent);
		detail.setUnits(BigDecimal.ONE);
		detail.setName(billDetailNameProvider.getName(detail));
		if (bill.getDetails() == null) {
			bill.setDetails(new ArrayList<BillDetail>());
		}
		bill.getDetails().add(detail);
	}

	private void addLiquidationConcept(Bill bill, BillConcept concept, BigDecimal percent, Map<BillConcept, BigDecimal> billingData) {
		BigDecimal total = billingData.get(concept);
		if (MathUtils.isNotZero(total)) {
			BigDecimal value = total.multiply(percent).divide(MathUtils.HUNDRED, 2, RoundingMode.HALF_EVEN);
			LiquidationDetail detail = new LiquidationDetail();
			detail.setId(UUID.randomUUID().toString());
			detail.setConcept(concept);
			detail.setValue(value);
			detail.setBill(bill);
			detail.setBaseValue(total);
			detail.setPercent(percent);
			detail.setUnits(BigDecimal.ONE);
			detail.setName(billDetailNameProvider.getName(detail));
			if (bill.getLiquidationDetails() == null) {
				bill.setLiquidationDetails(new ArrayList<LiquidationDetail>());
			}
			bill.getLiquidationDetails().add(detail);
		}
	}

	private void addLiquidationFixedConcept(Bill bill, BillConcept concept, BigDecimal value) {
		LiquidationDetail detail = new LiquidationDetail();
		detail.setId(UUID.randomUUID().toString());
		detail.setConcept(concept);
		detail.setValue(value);
		detail.setBill(bill);
		detail.setUnits(BigDecimal.ONE);
		detail.setName(i18nService.getMessage("bill.concept.name." + concept.name()));
		if (bill.getLiquidationDetails() == null) {
			bill.setLiquidationDetails(new ArrayList<LiquidationDetail>());
		}
		bill.getLiquidationDetails().add(detail);
	}

	private List<String> resolveTerminals(Store store) {
		List<String> result = new ArrayList<String>();
		for (TerminalRelation i : store.getTerminalRelations()) {
			result.add(i.getCode());
		}
		return result;
	}
}
