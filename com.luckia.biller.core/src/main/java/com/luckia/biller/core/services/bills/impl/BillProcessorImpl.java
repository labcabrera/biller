/*******************************************************************************
 * Copyright (c) 2004, 2012 Kotasoft S.L.
 * All rights reserved. This program and the accompanying materials
 * may only be used prior written consent of Kotasoft S.L.
 ******************************************************************************/
package com.luckia.biller.core.services.bills.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.persistence.EntityManager;

import org.apache.commons.lang3.Range;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.persist.Transactional;
import com.luckia.biller.core.common.MathUtils;
import com.luckia.biller.core.model.AppFile;
import com.luckia.biller.core.model.Bill;
import com.luckia.biller.core.model.BillDetail;
import com.luckia.biller.core.model.BillLiquidationDetail;
import com.luckia.biller.core.model.BillType;
import com.luckia.biller.core.model.CommonState;
import com.luckia.biller.core.model.Liquidation;
import com.luckia.biller.core.model.Store;
import com.luckia.biller.core.scheduler.tasks.LiquidationRecalculationTask;
import com.luckia.biller.core.services.AuditService;
import com.luckia.biller.core.services.FileService;
import com.luckia.biller.core.services.SettingsService;
import com.luckia.biller.core.services.StateMachineService;
import com.luckia.biller.core.services.bills.BillProcessor;
import com.luckia.biller.core.services.bills.LiquidationProcessor;
import com.luckia.biller.core.services.pdf.PDFBillGenerator;

/**
 * Implementacion de {@link BillProcessor}
 */
public class BillProcessorImpl implements BillProcessor {

	private static final Logger LOG = LoggerFactory.getLogger(BillProcessorImpl.class);

	@Inject
	private Provider<EntityManager> entityManagerProvider;
	@Inject
	private StateMachineService stateMachineService;
	@Inject
	private BillCodeGenerator billCodeGenerator;
	@Inject
	private BillDetailProcessor billDetailProcessor;
	@Inject
	private SettingsService settingsService;
	@Inject
	private FileService fileService;
	@Inject
	private PDFBillGenerator pdfBillGenerator;
	@Inject
	private AuditService auditService;
	@Inject
	private LiquidationProcessor liquidationProcessor;

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.luckia.biller.core.services.billing.BillProcessor#generateBill(com.luckia.biller.core.model.Store, org.apache.commons.lang3.Range)
	 */
	@Override
	@Transactional
	public Bill generateBill(Store store, Range<Date> range) {
		EntityManager entityManager = entityManagerProvider.get();
		Bill bill = new Bill();
		bill.setId(UUID.randomUUID().toString());
		bill.setBillDate(range.getMaximum());
		bill.setDetails(new ArrayList<BillDetail>());
		bill.setSender(store);
		bill.setModel(store.getBillingModel());
		bill.setDateFrom(range.getMinimum());
		bill.setDateTo(range.getMaximum());
		bill.setBillType(BillType.Common);
		if (store.getParent() != null) {
			bill.setReceiver(store.getParent());
		} else {
			throw new RuntimeException("No se puede generar la factura: no se ha definido la empresa del local " + store);
		}
		auditService.processCreated(bill);
		entityManager.persist(bill);
		stateMachineService.createTransition(bill, CommonState.Initial.name());
		return bill;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.luckia.biller.core.services.billing.BillProcessor#processDetails(com.luckia.biller.core.model.Bill)
	 */
	@Override
	@Transactional
	public void processDetails(Bill bill) {
		EntityManager entityManager = entityManagerProvider.get();
		billDetailProcessor.process(bill);
		for (BillDetail detail : bill.getDetails()) {
			entityManager.merge(detail);
		}
		entityManager.merge(bill);
		stateMachineService.createTransition(bill, CommonState.Draft.name());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.luckia.biller.core.services.billing.BillProcessor#processResults(com.luckia.biller.core.model.Bill)
	 */
	@Override
	@Transactional
	public void processResults(Bill bill) {
		BigDecimal netAmount = BigDecimal.ZERO;
		for (BillDetail detail : bill.getDetails()) {
			netAmount = netAmount.add(detail.getValue());
		}
		BigDecimal vatPercent = settingsService.getBillingSettings().getValue("vat", BigDecimal.class);
		BigDecimal vatAmount = netAmount.multiply(vatPercent).divide(MathUtils.HUNDRED, 2, RoundingMode.HALF_EVEN);
		BigDecimal amount = netAmount.add(vatAmount);
		bill.setNetAmount(netAmount);
		bill.setAmount(amount);
		bill.setVatPercent(vatPercent);
		bill.setVatAmount(vatAmount);

		// Procesamos la liquidacion
		BigDecimal betAmount = BigDecimal.ZERO;
		BigDecimal satAmount = BigDecimal.ZERO;
		BigDecimal otherAmount = BigDecimal.ZERO;
		BigDecimal adjustmentAmount = BigDecimal.ZERO;

		// Primero calculamos los conceptos de liquidacion
		for (BillLiquidationDetail detail : bill.getLiquidationDetails()) {
			if (detail.getConcept() != null) {
				BigDecimal partial = detail.getValue() != null ? detail.getValue() : BigDecimal.ZERO;
				switch (detail.getConcept()) {
				case Stakes:
				case GGR:
				case NGR:
				case NR:
					betAmount = betAmount.add(partial);
					break;
				case SatMonthlyFees:
				case CommercialMonthlyFees:
					satAmount = satAmount.add(partial);
					break;
				default:
					LOG.warn("Ignorando concepto no esperado en la liquidacion: {}", detail.getConcept());
					break;
				}
			}
		}
		// Despues comprobamos los conceptos de facturacion que aplican a la liquidacion
		for (BillDetail detail : bill.getDetails()) {
			if (detail.getConcept() != null) {
				BigDecimal partial = detail.getValue() != null ? detail.getValue() : BigDecimal.ZERO;
				switch (detail.getConcept()) {
				case ManualWithLiquidation:
					otherAmount = otherAmount.add(partial);
					break;
				case Adjustment:
					adjustmentAmount = adjustmentAmount.add(partial);
					break;
				default:
					break;
				}
			}
		}

		BigDecimal totalLiquidationAmount = betAmount.add(satAmount).add(otherAmount);
		LOG.debug("Bill liquidation amount: {}; Adjustment amount: {}", totalLiquidationAmount, adjustmentAmount);
		bill.setLiquidationBetAmount(betAmount);
		bill.setLiquidationSatAmount(satAmount);
		bill.setLiquidationOtherAmount(otherAmount);
		bill.setLiquidationTotalAmount(totalLiquidationAmount);
		bill.setAdjustmentAmount(adjustmentAmount);
		EntityManager entityManager = entityManagerProvider.get();
		entityManager.merge(bill);
		if (bill.getLiquidation() != null) {
			entityManager.flush();
			entityManager.clear();
			Liquidation liquidation = entityManager.find(Liquidation.class, bill.getLiquidation().getId());
			LOG.debug("Actualizando resultados de la liquidacion {}", liquidation);
			BigDecimal totalAmount = BigDecimal.ZERO;
			for (Bill i : liquidation.getBills()) {
				LOG.debug("Liquidacion de {}: {}", i.getSender(), i.getLiquidationTotalAmount());
				if (MathUtils.isNotZero(i.getLiquidationTotalAmount())) {
					totalAmount = totalAmount.add(i.getLiquidationTotalAmount());
				} else {
					LOG.warn("La liquidacion de {} es nula", i.getSender());
				}
			}
			liquidation.setAmount(totalAmount);
			LOG.debug("Resultado de la liquidacion: {}", totalAmount);
			entityManager.merge(liquidation);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.luckia.biller.core.services.bills.BillProcessor#confirmBill(com.luckia.biller.core.model.Bill)
	 */
	@Override
	@Transactional
	public void confirmBill(Bill bill) {
		try {
			EntityManager entityManager = entityManagerProvider.get();
			stateMachineService.createTransition(bill, CommonState.Confirmed.name());
			// NOTA: pudiera ser que la factura se ha regenerado, en cuyo caso mantenemos el mismo numero de factura anterior
			if (StringUtils.isBlank(bill.getCode())) {
				billCodeGenerator.generateCode(bill);
			}
			File tempFile = File.createTempFile("tmp-bill-", ".pdf");
			FileOutputStream out = new FileOutputStream(tempFile);
			pdfBillGenerator.generate(bill, out);
			out.close();
			FileInputStream in = new FileInputStream(tempFile);
			String name = String.format("bill-%s.pdf", bill.getCode());
			AppFile pdfFile = fileService.save(name, "application/pdf", in);
			bill.setPdfFile(pdfFile);
			entityManager.merge(bill);
		} catch (Exception ex) {
			throw new RuntimeException("Error al confirmar la factura", ex);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.luckia.biller.core.services.bills.BillProcessor#rectifyBill(com.luckia.biller.core.model.Bill)
	 */
	@Override
	@Transactional
	public Bill rectifyBill(Bill bill) {
		EntityManager entityManager = entityManagerProvider.get();
		stateMachineService.createTransition(bill, CommonState.Rectified.name());
		Bill rectified = new Bill();
		rectified.setId(UUID.randomUUID().toString());
		rectified.setBillDate(bill.getBillDate());
		rectified.setSender(bill.getSender());
		rectified.setReceiver(bill.getReceiver());
		rectified.setModel(bill.getModel());
		rectified.setDateFrom(bill.getDateFrom());
		rectified.setDateTo(bill.getDateTo());
		rectified.setBillType(BillType.Rectified);
		rectified.setDetails(new ArrayList<BillDetail>());
		rectified.setLiquidationDetails(new ArrayList<BillLiquidationDetail>());
		rectified.setParent(bill);
		auditService.processCreated(rectified);
		entityManager.persist(rectified);
		for (BillDetail detail : bill.getDetails()) {
			BillDetail copy = detail.clone();
			copy.setBill(rectified);
			copy.setId(UUID.randomUUID().toString());
			rectified.getDetails().add(copy);
			entityManager.persist(rectified);
		}
		entityManager.merge(rectified);
		stateMachineService.createTransition(rectified, CommonState.Draft.name());
		processResults(bill);
		return rectified;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.luckia.biller.core.services.bills.BillProcessor#mergeDetail(com.luckia.biller.core.model.BillDetail)
	 */
	@Override
	@Transactional
	public Bill mergeDetail(BillDetail detail) {
		EntityManager entityManager = entityManagerProvider.get();
		Boolean isNew = detail.getId() == null;
		detail.setValue(detail.getValue() != null ? detail.getValue().setScale(2, RoundingMode.HALF_EVEN) : null);
		detail.setUnits(detail.getUnits() != null ? detail.getUnits().setScale(2, RoundingMode.HALF_EVEN) : null);
		Bill bill;
		if (isNew) {
			bill = entityManager.find(Bill.class, detail.getBill().getId());
			detail.setId(UUID.randomUUID().toString());
			entityManager.persist(detail);
			bill.getDetails().add(detail);
		} else {
			BillDetail current = entityManager.find(BillDetail.class, detail.getId());
			current.merge(detail);
			entityManager.merge(current);
			bill = current.getBill();
		}
		entityManager.flush();
		entityManager.refresh(bill);
		processResults(bill);
		return bill;
	}

	@Override
	@Transactional
	public Bill removeDetail(BillDetail detail) {
		EntityManager entityManager = entityManagerProvider.get();
		Bill bill = detail.getBill();
		bill.getDetails().remove(detail);
		entityManager.remove(detail);
		entityManager.flush();
		bill = entityManager.find(Bill.class, detail.getBill().getId());
		entityManager.refresh(bill);
		processResults(bill);
		return bill;
	}

	@Override
	@Transactional
	public void remove(Bill bill) {
		LOG.info("Eliminando factura {}", bill);
		// Eliminamos los detalles
		EntityManager entityManager = entityManagerProvider.get();
		for (Object i : bill.getDetails()) {
			entityManager.remove(i);
		}
		for (Object i : bill.getLiquidationDetails()) {
			entityManager.remove(i);
		}
		Liquidation liquidation = null;
		if (bill.getLiquidation() != null) {
			liquidation = bill.getLiquidation();
		}
		entityManager.remove(bill);
		if (liquidation != null) {
			LOG.debug("Actualizando los detalles de la liquidacion {}", liquidation);
			entityManager.flush();
			LiquidationRecalculationTask task = new LiquidationRecalculationTask(liquidation.getId(), entityManagerProvider, liquidationProcessor);
			task.run();
		}
	}
}
