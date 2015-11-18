package com.luckia.biller.core.services.bills.impl;

import static org.apache.commons.lang3.time.DateFormatUtils.ISO_DATE_FORMAT;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.validation.ValidationException;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.Range;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Injector;
import com.google.inject.persist.Transactional;
import com.luckia.biller.core.common.MathUtils;
import com.luckia.biller.core.common.NoAvailableDataException;
import com.luckia.biller.core.common.RegisterActivity;
import com.luckia.biller.core.i18n.I18nService;
import com.luckia.biller.core.model.AppFile;
import com.luckia.biller.core.model.Bill;
import com.luckia.biller.core.model.BillingModel;
import com.luckia.biller.core.model.CommonState;
import com.luckia.biller.core.model.Company;
import com.luckia.biller.core.model.LegalEntity;
import com.luckia.biller.core.model.Liquidation;
import com.luckia.biller.core.model.LiquidationDetail;
import com.luckia.biller.core.model.LiquidationResults;
import com.luckia.biller.core.model.UserActivityType;
import com.luckia.biller.core.scheduler.tasks.LiquidationRecalculationTask;
import com.luckia.biller.core.services.AuditService;
import com.luckia.biller.core.services.FileService;
import com.luckia.biller.core.services.StateMachineService;
import com.luckia.biller.core.services.bills.LiquidationProcessor;
import com.luckia.biller.core.services.entities.ProvinceTaxesService;
import com.luckia.biller.core.services.pdf.PDFLiquidationGenerator;

/**
 * Implementación de {@link LiquidationProcessor}
 */
public class LiquidationProcessorImpl implements LiquidationProcessor {

	private static final Logger LOG = LoggerFactory.getLogger(LiquidationProcessorImpl.class);

	@Inject
	private Provider<EntityManager> entityManagerProvider;
	@Inject
	private StateMachineService stateMachineService;
	@Inject
	private PDFLiquidationGenerator pdfLiquidationGenerator;
	@Inject
	private FileService fileService;
	@Inject
	private LiquidationCodeGenerator liquidationCodeGenerator;
	@Inject
	private AuditService auditService;
	@Inject
	private I18nService i18nService;
	@Inject
	private LiquidationReceiverProvider liquidationReceiverProvider;
	@Inject
	private Injector injector;
	@Inject
	private ProvinceTaxesService provincesTaxesService;

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.luckia.biller.core.services.bills.impl.LiquidationProcessor#process(com.luckia.biller.core.model.Company, org.apache.commons.lang3.Range)
	 */
	@Override
	@Transactional
	public Liquidation processBills(Company company, Range<Date> range) {
		LOG.debug("Procesando liquidacion de {} en {}", company.getName(), ISO_DATE_FORMAT.format(range.getMinimum()), ISO_DATE_FORMAT.format(range.getMaximum()));
		EntityManager entityManager = entityManagerProvider.get();
		TypedQuery<Bill> query = entityManager.createNamedQuery("Bill.selectPendingByReceiverInRange", Bill.class);
		query.setParameter("receiver", company);
		query.setParameter("from", range.getMinimum());
		query.setParameter("to", range.getMaximum());
		List<Bill> bills = query.getResultList();
		if (bills.isEmpty()) {
			throw new NoAvailableDataException();
		}
		LOG.debug("Encontradas {} facturas pendientes asociadas a la liquidacion", bills.size());
		BillingModel model = bills.iterator().next().getModel();
		LegalEntity LiquidationReceiver = model.getReceiver() != null ? model.getReceiver() : liquidationReceiverProvider.getReceiver();
		Liquidation liquidation = new Liquidation();
		liquidation.setModel(model);
		liquidation.setSender(company);
		liquidation.setReceiver(LiquidationReceiver);
		liquidation.setBills(bills);
		liquidation.setDateFrom(range.getMinimum());
		liquidation.setDateTo(range.getMaximum());
		liquidation.setBillDate(range.getMaximum());
		liquidation.setModel(company.getBillingModel());
		auditService.processCreated(liquidation);
		processResults(liquidation);
		for (Bill bill : liquidation.getBills()) {
			bill.setLiquidation(liquidation);
			entityManager.merge(bill);
		}
		entityManager.merge(liquidation);
		LOG.debug("Procesada la liquidacion de {}. Creando transicion a estado borrador", company.getName());
		stateMachineService.createTransition(liquidation, CommonState.Draft.name());
		return liquidation;
	}

	@Override
	public void processResults(Liquidation liquidation) {
		BigDecimal netAmount = BigDecimal.ZERO;
		BigDecimal vatAmount = BigDecimal.ZERO;
		BigDecimal totalAmount = BigDecimal.ZERO;
		BigDecimal storeCash = BigDecimal.ZERO;
		BigDecimal liquidationManualAmount = BigDecimal.ZERO;
		BigDecimal storeManualOuterAmount = BigDecimal.ZERO;
		BigDecimal liquidationOuterAmount = BigDecimal.ZERO;
		BigDecimal storeAmount = BigDecimal.ZERO; // TODO no se si esto sigue teniendo sentido
		for (Bill bill : liquidation.getBills()) {
			netAmount = netAmount.add(MathUtils.safeNull(bill.getLiquidationTotalNetAmount()));
			vatAmount = vatAmount.add(MathUtils.safeNull(bill.getLiquidationTotalVat()));
			totalAmount = totalAmount.add(MathUtils.safeNull(bill.getLiquidationTotalAmount()));
			storeCash = storeCash.add(MathUtils.safeNull(bill.getStoreCash()));
			storeManualOuterAmount = storeManualOuterAmount.add(MathUtils.safeNull(bill.getLiquidationOuterAmount()));
			if (bill.getModel() != null && BooleanUtils.isTrue(bill.getModel().getIncludeStores())) {
				storeAmount = storeAmount.add(bill.getAmount());
			}
		}
		if (liquidation.getDetails() != null) {
			for (LiquidationDetail detail : liquidation.getDetails()) {
				if (detail.getLiquidationIncluded()) {
					netAmount = netAmount.add(MathUtils.safeNull(detail.getNetValue()));
					vatAmount = vatAmount.add(MathUtils.safeNull(detail.getVatValue()));
					totalAmount = totalAmount.add(MathUtils.safeNull(detail.getValue()));
					liquidationManualAmount = liquidationManualAmount.add(MathUtils.safeNull(detail.getValue()));
				} else {
					liquidationOuterAmount = liquidationOuterAmount.add(detail.getValue());
				}
			}
		}
		LiquidationResults results = liquidation.getLiquidationResults();
		if (results == null) {
			results = new LiquidationResults();
			liquidation.setLiquidationResults(results);
		}
		results.setStoreAmount(storeAmount);
		results.setStoreManualOuterAmount(storeManualOuterAmount);
		results.setTotalOuterAmount(storeManualOuterAmount.add(liquidationOuterAmount));
		results.setAdjustmentAmount(liquidationManualAmount);
		results.setCashStoreAmount(storeCash);
		results.setCashStoreAdjustmentAmount(storeCash.add(storeManualOuterAmount).add(liquidationOuterAmount));
		results.setNetAmount(netAmount);
		results.setVatAmount(vatAmount);
		results.setTotalAmount(totalAmount);
		results.setReceiverAmount(storeCash.subtract(totalAmount));
		results.setEffectiveLiquidationAmount(results.getReceiverAmount().add(results.getTotalOuterAmount()));
		results.setSenderAmount(results.getCashStoreAdjustmentAmount().subtract(results.getEffectiveLiquidationAmount()));
	}

	@Override
	@Transactional
	public Liquidation updateLiquidationResults(Liquidation liquidation) {
		LOG.debug("Actualizando resultados de liquidacion");
		EntityManager entityManager = entityManagerProvider.get();
		entityManager.clear();
		Liquidation current = entityManager.find(Liquidation.class, liquidation.getId());
		processResults(liquidation);
		entityManager.merge(liquidation);
		return current;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.luckia.biller.core.services.bills.LiquidationProcessor#confirm(com.luckia.biller.core.model.Liquidation)
	 */
	@Override
	@Transactional
	public void confirm(Liquidation liquidation) {
		preValidateConfirmLiquidation(liquidation);
		try {
			EntityManager entityManager = entityManagerProvider.get();
			stateMachineService.createTransition(liquidation, CommonState.Confirmed.name());
			// NOTA: pudiera ser que la liquidacion se ha regenerado, en cuyo caso mantenemos el mismo numero de liquidacion
			if (StringUtils.isBlank(liquidation.getCode())) {
				liquidationCodeGenerator.generateCode(liquidation);
			}
			File tempFile = File.createTempFile("tmp-bill-", ".pdf");
			FileOutputStream out = new FileOutputStream(tempFile);
			pdfLiquidationGenerator.generate(liquidation, out);
			out.close();
			FileInputStream in = new FileInputStream(tempFile);
			String name = String.format("bill-%s.pdf", liquidation.getId());
			AppFile pdfFile = fileService.save(name, "application/pdf", in);
			liquidation.setPdfFile(pdfFile);
			entityManager.merge(liquidation);
		} catch (IOException ex) {
			throw new RuntimeException("Error al confirmar la factura", ex);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.luckia.biller.core.services.bills.LiquidationProcessor#mergeDetail(com.luckia.biller.core.model.LiquidationDetail)
	 */
	@Override
	@Transactional
	@RegisterActivity(type = UserActivityType.LIQUIDATION_MERGE_DETAIL)
	public Liquidation mergeDetail(LiquidationDetail detail) {
		EntityManager entityManager = entityManagerProvider.get();
		Boolean isNew = detail.getId() == null;
		detail.setValue(detail.getValue() != null ? detail.getValue().setScale(2, RoundingMode.HALF_EVEN) : null);
		detail.setUnits(detail.getUnits() != null ? detail.getUnits().setScale(2, RoundingMode.HALF_EVEN) : null);
		Liquidation liquidation = entityManager.find(Liquidation.class, detail.getLiquidation().getId());
		// Obtenemos el modelo de facturacion. Como esta asociado a los establecimientos asumimos que la parte de tratamiento del IVA es
		// comun a todos y leemos el primer registro. A partir de este valor hacemos el calculo del IVA
		BillingModel model = liquidation.getModel();
		if (model == null) {
			model = liquidation.getBills().iterator().next().getModel();
			liquidation.setModel(model);
			entityManager.merge(liquidation);
		}
		BigDecimal sourceValue = detail.getValue();
		BigDecimal vatPercent = BigDecimal.ZERO;
		BigDecimal netValue = BigDecimal.ZERO;
		BigDecimal vatValue = BigDecimal.ZERO;
		BigDecimal value = BigDecimal.ZERO;
		if (detail.getLiquidationIncluded()) {
			switch (model.getVatLiquidationType()) {
			case LIQUIDATION_INCLUDED:
				vatPercent = provincesTaxesService.getVatPercent(liquidation);
				BigDecimal divisor = MathUtils.HUNDRED.add(vatPercent).divide(MathUtils.HUNDRED);
				netValue = sourceValue.divide(divisor, 2, RoundingMode.HALF_EVEN);
				vatValue = sourceValue.subtract(netValue);
				value = netValue.add(vatValue);
				break;
			case LIQUIDATION_ADDED:
				vatPercent = provincesTaxesService.getVatPercent(liquidation);
				netValue = sourceValue;
				vatValue = netValue.multiply(vatPercent).divide(MathUtils.HUNDRED, 2, RoundingMode.HALF_EVEN);
				value = netValue.add(vatValue);
				break;
			default:
				break;
			}
		} else {
			netValue = sourceValue;
			value = sourceValue;
		}
		detail.setSourceValue(sourceValue);
		detail.setNetValue(netValue);
		detail.setVatValue(vatValue);
		detail.setValue(value);
		if (isNew) {
			detail.setId(UUID.randomUUID().toString());
			entityManager.persist(detail);
			liquidation.getDetails().add(detail);
		} else {
			LiquidationDetail current = entityManager.find(LiquidationDetail.class, detail.getId());
			current.merge(detail);
			entityManager.merge(current);
			entityManager.flush();
			liquidation = current.getLiquidation();
		}
		return updateLiquidationResults(liquidation);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.luckia.biller.core.services.bills.LiquidationProcessor#removeDetail(com.luckia.biller.core.model.LiquidationDetail)
	 */
	@Override
	@Transactional
	@RegisterActivity(type = UserActivityType.LIQUIDATION_REMOVE_DETAIL)
	public Liquidation removeDetail(LiquidationDetail detail) {
		LOG.debug("Eliminando detalle de liquidacion");
		EntityManager entityManager = entityManagerProvider.get();
		LiquidationDetail currentDetail = entityManager.find(LiquidationDetail.class, detail.getId());
		Liquidation currentLiquidation = currentDetail.getLiquidation();
		currentDetail.getLiquidation().getDetails().remove(currentDetail);
		entityManager.remove(currentDetail);
		entityManager.flush();
		entityManager.refresh(currentLiquidation);
		return updateLiquidationResults(currentLiquidation);
	}

	@Override
	@Transactional
	@RegisterActivity(type = UserActivityType.LIQUIDATION_REMOVE)
	public void remove(Liquidation liquidation) {
		EntityManager entityManager = entityManagerProvider.get();
		entityManager.createQuery("update Bill e set e.liquidation = null where e.liquidation = :liquidation").setParameter("liquidation", liquidation).executeUpdate();
		entityManager.createQuery("delete from LiquidationDetail e where e.liquidation = :liquidation").setParameter("liquidation", liquidation).executeUpdate();
		entityManager.remove(liquidation);
	}

	/**
	 * Comprobamos que todas las facturas de la liquidacion han sido aceptadas. En caso de que alguna factura no esté aceptada eleva un {@link ValidationException}
	 * 
	 * @param liquidation
	 */
	private void preValidateConfirmLiquidation(Liquidation liquidation) {
		for (Bill i : liquidation.getBills()) {
			if (CommonState.Draft.name().equals(i.getCurrentState().getStateDefinition().getId())) {
				throw new ValidationException(i18nService.getMessage("liquidation.confirm.error.unconfirmedBills"));
			}
		}
	}

	@Override
	@Transactional
	public Liquidation recalculate(String liquidationId) {
		LiquidationRecalculationTask task = new LiquidationRecalculationTask(liquidationId, injector);
		task.run();
		return task.getLiquidationResult();
	}
}
