package com.luckia.biller.core.services.bills.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import javax.inject.Inject;

import org.apache.commons.lang3.Range;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.luckia.biller.core.common.MathUtils;
import com.luckia.biller.core.i18n.I18nService;
import com.luckia.biller.core.model.Bill;
import com.luckia.biller.core.model.BillConcept;
import com.luckia.biller.core.model.BillDetail;
import com.luckia.biller.core.model.BillLiquidationDetail;
import com.luckia.biller.core.model.BillRawData;
import com.luckia.biller.core.model.BillingModel;
import com.luckia.biller.core.model.BillingModelAttributes;
import com.luckia.biller.core.model.Store;
import com.luckia.biller.core.model.TerminalRelation;
import com.luckia.biller.core.services.bills.BillDataProvider;
import com.luckia.biller.core.services.entities.ProvinceTaxesService;

/**
 * Servicio encargado de crear los detalles de una factura a partir de los datos obtenidos a traves de {@link BillDataProvider}. Esto genera dos listas de conceptos:
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
	@Inject
	private ProvinceTaxesService provinceTaxesService;

	/**
	 * CUIDADO: los importes que provienen de LIS tienen IVA, de modo que para hacer el calculo debemos volver a calcular la base
	 * 
	 * @param bill
	 */
	public void process(Bill bill) {
		Store store = bill.getSender(Store.class);
		BillingModel model = bill.getModel();
		List<String> terminals = resolveTerminals(store);
		if (terminals.isEmpty()) {
			LOG.info("No se puede generar la factura de {}. No tiene terminales asociados.", store.getName());
		} else {
			Range<Date> range = Range.between(bill.getDateFrom(), bill.getDateTo());
			Map<BillConcept, BigDecimal> billingData = billingDataProvider.retreive(bill, range, terminals);
			bill.setBillRawData(new ArrayList<BillRawData>());
			for (Entry<BillConcept, BigDecimal> entry : billingData.entrySet()) {
				bill.getBillRawData().add(new BillRawData(bill, entry.getKey(), entry.getValue()));
			}
			BigDecimal vatPercent = provinceTaxesService.getVatPercent(bill);
			BigDecimal vatDivisor = BigDecimal.ONE.add(vatPercent.divide(MathUtils.HUNDRED, 2, RoundingMode.HALF_EVEN));

			if (model.getStoreModel() == null) {
				model.setStoreModel(new BillingModelAttributes());
			}

			// Calculamos los conceptos de la facturacion.
			BigDecimal stakes = billingData.containsKey(BillConcept.STAKES) ? billingData.get(BillConcept.STAKES).divide(vatDivisor, 2, RoundingMode.HALF_EVEN) : null;
			if (MathUtils.isNotZero(stakes)) {
				if (MathUtils.isNotZero(model.getStoreModel().getStakesPercent())) {
					BigDecimal percent = model.getStoreModel().getStakesPercent();
					BigDecimal value = stakes.multiply(percent).divide(MathUtils.HUNDRED, 2, RoundingMode.HALF_EVEN);
					addBillConcept(bill, BillConcept.STAKES, value, stakes, percent);
				}

				// Calculamos los conceptos de la liquidacion definidos a nivel de los porcentajes de las variables del terminal:
				Map<BillConcept, BigDecimal> percentConcepts = new LinkedHashMap<BillConcept, BigDecimal>();
				percentConcepts.put(BillConcept.NGR, model.getCompanyModel().getNgrPercent());
				percentConcepts.put(BillConcept.GGR, model.getCompanyModel().getGgrPercent());
				percentConcepts.put(BillConcept.NR, model.getCompanyModel().getNrPercent());
				percentConcepts.put(BillConcept.STAKES, model.getCompanyModel().getStakesPercent());
				for (BillConcept concept : percentConcepts.keySet()) {
					BigDecimal detailPercent = percentConcepts.get(concept);
					if (MathUtils.isNotZero(detailPercent)) {
						addLiquidationPercentConcept(bill, concept, detailPercent, billingData, vatPercent);
					}
				}
				// Calculamos los conceptos fijos
				processLiquidationFixedConcepts(bill, model, range, terminals);
			} else {
				LOG.debug("No generamos resultados de liquidacion de {}: carece de operaciones en el rango de facturacion", store.getName());
			}
			// Almacenamos el saldo de caja
			bill.setStoreCash(billingData.containsKey(BillConcept.STORE_CASH) ? billingData.get(BillConcept.STORE_CASH).setScale(2, RoundingMode.HALF_EVEN) : BigDecimal.ZERO);
		}
	}

	private void processLiquidationFixedConcepts(Bill bill, BillingModel model, Range<Date> range, List<String> terminals) {
		Map<BillConcept, BigDecimal> fixedConcepts = new LinkedHashMap<BillConcept, BigDecimal>();
		fixedConcepts.put(BillConcept.COMMERCIAL_MONTHLY_FEES, model.getCompanyModel().getCommercialMonthlyFees());
		fixedConcepts.put(BillConcept.SAT_MONTHLY_FEES, model.getCompanyModel().getSatMonthlyFees());
		for (BillConcept concept : fixedConcepts.keySet()) {
			BigDecimal value = fixedConcepts.get(concept);
			if (MathUtils.isNotZeroPositive(value)) {
				addLiquidationFixedConcept(bill, concept, value);
			}
		}
		if (MathUtils.isNotZero(model.getCompanyModel().getPricePerLocation())) {
			addLiquidationFixedConcept(bill, BillConcept.PRICE_PER_LOCATION, model.getCompanyModel().getPricePerLocation().negate());
		}
	}

	private void addLiquidationPercentConcept(Bill bill, BillConcept concept, BigDecimal total, BigDecimal percent, BigDecimal vatPercent) {
		if (MathUtils.isNotZero(total)) {
			BigDecimal netValue = total.multiply(percent).divide(MathUtils.HUNDRED, 2, RoundingMode.HALF_EVEN);
			BillLiquidationDetail detail = new BillLiquidationDetail();
			detail.setId(UUID.randomUUID().toString());
			detail.setConcept(concept);
			detail.setNetValue(netValue);
			detail.setBill(bill);
			detail.setSourceValue(total);
			detail.setPercent(percent);
			detail.setUnits(BigDecimal.ONE);
			detail.setName(billDetailNameProvider.getName(detail));
			processVatDetail(detail, bill, percent);
			detail.setLiquidationIncluded(true);
			if (bill.getLiquidationDetails() == null) {
				bill.setLiquidationDetails(new ArrayList<BillLiquidationDetail>());
			}
			bill.getLiquidationDetails().add(detail);
		}
	}

	private void addLiquidationPercentConcept(Bill bill, BillConcept concept, BigDecimal percent, Map<BillConcept, BigDecimal> data, BigDecimal vatPercent) {
		BigDecimal total = data.get(concept);
		if (MathUtils.isNotZero(total)) {
			addLiquidationPercentConcept(bill, concept, total, percent, vatPercent);
		}
	}

	private void addLiquidationFixedConcept(Bill bill, BillConcept concept, BigDecimal value) {
		BillLiquidationDetail detail = new BillLiquidationDetail();
		detail.setId(UUID.randomUUID().toString());
		detail.setLiquidationIncluded(true);
		detail.setConcept(concept);
		detail.setBill(bill);
		detail.setUnits(BigDecimal.ONE);
		detail.setName(i18nService.getMessage("bill.concept.name." + concept.name()));
		detail.setSourceValue(value);
		processVatDetail(detail, bill, MathUtils.HUNDRED);
		if (bill.getLiquidationDetails() == null) {
			bill.setLiquidationDetails(new ArrayList<BillLiquidationDetail>());
		}
		bill.getLiquidationDetails().add(detail);
	}

	private void addBillConcept(Bill bill, BillConcept concept, BigDecimal value, BigDecimal total, BigDecimal percent) {
		BillDetail detail = new BillDetail();
		detail.setId(UUID.randomUUID().toString());
		detail.setConcept(concept);
		detail.setSourceValue(total);
		detail.setValue(value);
		detail.setBill(bill);
		detail.setPercent(percent);
		detail.setUnits(BigDecimal.ONE);
		detail.setName(billDetailNameProvider.getName(detail));
		if (bill.getBillDetails() == null) {
			bill.setBillDetails(new ArrayList<BillDetail>());
		}
		bill.getBillDetails().add(detail);
	}

	private List<String> resolveTerminals(Store store) {
		List<String> result = new ArrayList<String>();
		if (store.getTerminalRelations() != null) {
			for (TerminalRelation i : store.getTerminalRelations()) {
				result.add(i.getCode());
			}
		}
		return result;
	}

	private void processVatDetail(BillLiquidationDetail detail, Bill bill, BigDecimal percent) {
		Validate.notNull(detail.getSourceValue());
		BigDecimal vatPercent = new BigDecimal("21"); // provinceTaxesService.getVatPercent(bill);
		BigDecimal sourceValue = detail.getSourceValue();
		BigDecimal effectiveSource = sourceValue.multiply(percent).divide(MathUtils.HUNDRED, 2, RoundingMode.HALF_EVEN);
		BillingModel model = bill.getModel();
		switch (model.getVatLiquidationType()) {
		case EXCLUDED:
			detail.setVatPercent(BigDecimal.ZERO);
			detail.setVatValue(BigDecimal.ZERO);
			detail.setValue(effectiveSource);
			detail.setNetValue(effectiveSource);
			break;
		case LIQUIDATION_INCLUDED:
			BigDecimal divisor = MathUtils.HUNDRED.add(vatPercent).divide(MathUtils.HUNDRED);
			BigDecimal baseImponible = effectiveSource.divide(divisor, 2, RoundingMode.HALF_EVEN);
			BigDecimal vatValue = effectiveSource.subtract(baseImponible);
			detail.setVatPercent(vatPercent);
			detail.setVatValue(vatValue);
			detail.setValue(effectiveSource);
			detail.setNetValue(baseImponible);
			break;
		case LIQUIDATION_ADDED:
			detail.setVatPercent(vatPercent);
			detail.setVatValue(effectiveSource.multiply(vatPercent).divide(MathUtils.HUNDRED, 2, RoundingMode.HALF_EVEN));
			detail.setValue(detail.getNetValue().add(detail.getVatValue()));
			detail.setNetValue(effectiveSource);
			break;
		}
	}
}
