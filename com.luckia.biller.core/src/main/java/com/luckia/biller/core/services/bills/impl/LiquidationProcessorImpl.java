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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.persist.Transactional;
import com.luckia.biller.core.i18n.I18nService;
import com.luckia.biller.core.model.AppFile;
import com.luckia.biller.core.model.Bill;
import com.luckia.biller.core.model.CommonState;
import com.luckia.biller.core.model.Company;
import com.luckia.biller.core.model.LegalEntity;
import com.luckia.biller.core.model.Liquidation;
import com.luckia.biller.core.model.LiquidationDetail;
import com.luckia.biller.core.model.LiquidationResults;
import com.luckia.biller.core.services.AuditService;
import com.luckia.biller.core.services.FileService;
import com.luckia.biller.core.services.StateMachineService;
import com.luckia.biller.core.services.bills.LiquidationProcessor;
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
		LOG.debug("Encontradas {} facturas pendientes asociadas a la liquidacion", bills.size());
		LegalEntity egasa = liquidationReceiverProvider.getReceiver();
		Liquidation liquidation = new Liquidation();
		liquidation.setId(UUID.randomUUID().toString());
		liquidation.setSender(company);
		liquidation.setReceiver(egasa);
		liquidation.setBills(bills);
		liquidation.setDateFrom(range.getMinimum());
		liquidation.setDateTo(range.getMaximum());
		liquidation.setBillDate(range.getMaximum());
		liquidation.setModel(company.getBillingModel());
		auditService.processCreated(liquidation);
		internalProcessResults(liquidation);
		for (Bill bill : liquidation.getBills()) {
			bill.setLiquidation(liquidation);
			entityManager.merge(bill);
		}
		entityManager.merge(liquidation);
		stateMachineService.createTransition(liquidation, CommonState.Draft.name());
		return liquidation;
	}

	private void internalProcessResults(Liquidation liquidation) {
		BigDecimal betAmount = BigDecimal.ZERO;
		BigDecimal satAmount = BigDecimal.ZERO;
		BigDecimal otherBillAmount = BigDecimal.ZERO;
		BigDecimal storeAmount = BigDecimal.ZERO;
		BigDecimal adjustmentsAmount = BigDecimal.ZERO;
		BigDecimal cashStore = BigDecimal.ZERO;
		for (Bill bill : liquidation.getBills()) {
			if (bill.getModel() == null) {
				LOG.warn("Ignorando factura sin modelo de facturacion asociado (posiblemente no este definida a nivel de establecimiento)");
				continue;
			}
			betAmount = betAmount.add(bill.getLiquidationBetAmount() != null ? bill.getLiquidationBetAmount() : BigDecimal.ZERO);
			satAmount = satAmount.add(bill.getLiquidationSatAmount() != null ? bill.getLiquidationSatAmount() : BigDecimal.ZERO);
			otherBillAmount = otherBillAmount.add(bill.getLiquidationOtherAmount() != null ? bill.getLiquidationOtherAmount() : BigDecimal.ZERO);
			adjustmentsAmount = adjustmentsAmount.add(bill.getAdjustmentAmount() != null ? bill.getAdjustmentAmount() : BigDecimal.ZERO);
			cashStore = cashStore.add(bill.getStoreCash() != null ? bill.getStoreCash() : BigDecimal.ZERO);
			if (bill.getModel() != null && BooleanUtils.isTrue(bill.getModel().getIncludeStores())) {
				storeAmount = storeAmount.add(bill.getAmount());
			}
		}
		LiquidationResults results = liquidation.getLiquidationResults();
		if (results == null) {
			results = new LiquidationResults();
			liquidation.setLiquidationResults(results);
		}
		results.setBetAmount(betAmount);
		results.setStoreAmount(storeAmount);
		results.setSatAmount(satAmount);
		results.setOtherAmount(otherBillAmount);
		results.setAdjustmentAmount(adjustmentsAmount);
		results.setAdjustmentSharedAmount(adjustmentsAmount.divide(new BigDecimal("2"), 2, RoundingMode.HALF_EVEN));
		results.setCashStoreAmount(cashStore);
		results.setCashStoreAdjustmentAmount(cashStore.add(adjustmentsAmount));

		BigDecimal senderAmount = betAmount.add(satAmount).add(otherBillAmount).add(results.getAdjustmentSharedAmount());
		if (liquidation.getDetails() != null) {
			for (LiquidationDetail detail : liquidation.getDetails()) {
				senderAmount = senderAmount.add(detail.getValue() != null ? detail.getValue() : BigDecimal.ZERO);
			}
		}
		results.setSenderAmount(senderAmount);
		results.setReceiverAmount(results.getCashStoreAdjustmentAmount().subtract(senderAmount));
	}

	@Override
	public Liquidation updateResults(Liquidation liquidation) {
		LOG.debug("Actualizando resultados de liquidacion");
		EntityManager entityManager = entityManagerProvider.get();
		entityManager.clear();
		Liquidation current = entityManager.find(Liquidation.class, liquidation.getId());
		entityManager.getTransaction().begin();
		internalProcessResults(liquidation);
		entityManager.merge(liquidation);
		entityManager.getTransaction().commit();
		return current;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.luckia.biller.core.services.bills.LiquidationProcessor#confirm(com.luckia.biller.core.model.Liquidation)
	 */
	@Override
	public void confirm(Liquidation liquidation) {
		preValidateConfirmLiquidation(liquidation);
		try {
			EntityManager entityManager = entityManagerProvider.get();
			entityManager.getTransaction().begin();
			stateMachineService.createTransition(liquidation, CommonState.Confirmed.name());
			liquidationCodeGenerator.generateCode(liquidation);
			File tempFile = File.createTempFile("tmp-bill-", ".pdf");
			FileOutputStream out = new FileOutputStream(tempFile);
			pdfLiquidationGenerator.generate(liquidation, out);
			out.close();
			FileInputStream in = new FileInputStream(tempFile);
			String name = String.format("bill-%s.pdf", liquidation.getId());
			AppFile pdfFile = fileService.save(name, "application/pdf", in);
			liquidation.setPdfFile(pdfFile);
			entityManager.merge(liquidation);
			entityManager.getTransaction().commit();
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
	public Liquidation mergeDetail(LiquidationDetail detail) {
		EntityManager entityManager = entityManagerProvider.get();
		entityManager.getTransaction().begin();
		Boolean isNew = detail.getId() == null;
		detail.setValue(detail.getValue() != null ? detail.getValue().setScale(2, RoundingMode.HALF_EVEN) : null);
		detail.setUnits(detail.getUnits() != null ? detail.getUnits().setScale(2, RoundingMode.HALF_EVEN) : null);
		Liquidation liquidation;
		if (isNew) {
			liquidation = entityManager.find(Liquidation.class, detail.getLiquidation().getId());
			detail.setId(UUID.randomUUID().toString());
			entityManager.persist(detail);
			liquidation.getDetails().add(detail);
		} else {
			LiquidationDetail current = entityManager.find(LiquidationDetail.class, detail.getId());
			current.merge(detail);
			entityManager.merge(current);
			liquidation = current.getLiquidation();
		}
		entityManager.getTransaction().commit();
		return updateResults(liquidation);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.luckia.biller.core.services.bills.LiquidationProcessor#removeDetail(com.luckia.biller.core.model.LiquidationDetail)
	 */
	@Override
	public Liquidation removeDetail(LiquidationDetail detail) {
		EntityManager entityManager = entityManagerProvider.get();
		Liquidation liquidation = detail.getLiquidation();
		liquidation.getDetails().remove(detail);
		entityManager.getTransaction().begin();
		entityManager.remove(detail);
		entityManager.getTransaction().commit();
		return updateResults(liquidation);
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
}
