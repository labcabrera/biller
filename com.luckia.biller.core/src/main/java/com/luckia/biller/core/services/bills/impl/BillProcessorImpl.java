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
import javax.persistence.EntityManager;

import org.apache.commons.lang3.Range;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.luckia.biller.core.common.MathUtils;
import com.luckia.biller.core.jpa.EntityManagerProvider;
import com.luckia.biller.core.model.AppFile;
import com.luckia.biller.core.model.Bill;
import com.luckia.biller.core.model.BillDetail;
import com.luckia.biller.core.model.BillLiquidationDetail;
import com.luckia.biller.core.model.BillType;
import com.luckia.biller.core.model.CommonState;
import com.luckia.biller.core.model.Liquidation;
import com.luckia.biller.core.model.Store;
import com.luckia.biller.core.services.AuditService;
import com.luckia.biller.core.services.FileService;
import com.luckia.biller.core.services.SettingsService;
import com.luckia.biller.core.services.StateMachineService;
import com.luckia.biller.core.services.bills.BillProcessor;
import com.luckia.biller.core.services.pdf.PDFBillGenerator;

public class BillProcessorImpl implements BillProcessor {

	private static final Logger LOG = LoggerFactory.getLogger(BillProcessorImpl.class);

	@Inject
	private EntityManagerProvider entityManagerProvider;
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.luckia.biller.core.services.billing.BillProcessor#generateBill(com.luckia.biller.core.model.Store,
	 * org.apache.commons.lang3.Range)
	 */
	@Override
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
		entityManager.getTransaction().begin();
		entityManager.persist(bill);
		stateMachineService.createTransition(bill, CommonState.Initial.name());
		entityManager.getTransaction().commit();
		return bill;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.luckia.biller.core.services.billing.BillProcessor#processDetails(com.luckia.biller.core.model.Bill)
	 */
	@Override
	public void processDetails(Bill bill) {
		billDetailProcessor.process(bill);
		EntityManager entityManager = entityManagerProvider.get();
		entityManager.getTransaction().begin();
		for (BillDetail detail : bill.getDetails()) {
			entityManager.merge(detail);
		}
		entityManager.merge(bill);
		stateMachineService.createTransition(bill, CommonState.Draft.name());
		entityManager.getTransaction().commit();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.luckia.biller.core.services.billing.BillProcessor#processResults(com.luckia.biller.core.model.Bill)
	 */
	@Override
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

		// Sumamos todos los conceptos de liquidacion y los ajustes operativos que han de propagarse a la liquidacion para obtener el valor
		BigDecimal liquidationAmount = BigDecimal.ZERO;
		for (BillLiquidationDetail detail : bill.getLiquidationDetails()) {
			liquidationAmount = liquidationAmount.add(detail.getValue() != null ? detail.getValue() : BigDecimal.ZERO);
		}
		for (BillDetail detail : bill.getDetails()) {
			if (detail.getPropagate() != null && detail.getPropagate()) {
				liquidationAmount = liquidationAmount.add(detail.getValue() != null ? detail.getValue() : BigDecimal.ZERO);
			}
		}
		LOG.debug("Bill liquidation amount: {}", liquidationAmount);
		bill.setLiquidationAmount(liquidationAmount);
		EntityManager entityManager = entityManagerProvider.get();
		entityManager.getTransaction().begin();
		entityManager.merge(bill);
		if (bill.getLiquidation() != null) {
			entityManager.flush();
			entityManager.clear();
			Liquidation liquidation = entityManager.find(Liquidation.class, bill.getLiquidation().getId());
			LOG.debug("Actualizando resultados de la liquidacion {}", liquidation);
			BigDecimal totalAmount = BigDecimal.ZERO;
			for (Bill i : liquidation.getBills()) {
				LOG.debug("Liquidacion de {}: {}", i.getSender(), i.getLiquidationAmount());
				totalAmount = totalAmount.add(i.getLiquidationAmount());
			}
			liquidation.setAmount(totalAmount);
			LOG.debug("Resultado de la liquidacion: {}", totalAmount);
			entityManager.merge(liquidation);
		}
		entityManager.getTransaction().commit();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.luckia.biller.core.services.bills.BillProcessor#confirmBill(com.luckia.biller.core.model.Bill)
	 */
	@Override
	public void confirmBill(Bill bill) {
		try {
			EntityManager entityManager = entityManagerProvider.get();
			entityManager.getTransaction().begin();
			stateMachineService.createTransition(bill, CommonState.Confirmed.name());
			billCodeGenerator.generateCode(bill);
			File tempFile = File.createTempFile("tmp-bill-", ".pdf");
			FileOutputStream out = new FileOutputStream(tempFile);
			pdfBillGenerator.generate(bill, out);
			out.close();
			FileInputStream in = new FileInputStream(tempFile);
			String name = String.format("bill-%s.pdf", bill.getCode());
			AppFile pdfFile = fileService.save(name, "application/pdf", in);
			bill.setPdfFile(pdfFile);
			entityManager.merge(bill);
			entityManager.getTransaction().commit();
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
	public Bill rectifyBill(Bill bill) {
		EntityManager entityManager = entityManagerProvider.get();
		entityManager.getTransaction().begin();
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
		entityManager.getTransaction().commit();
		processResults(bill);
		return rectified;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.luckia.biller.core.services.bills.BillProcessor#mergeDetail(com.luckia.biller.core.model.BillDetail)
	 */
	@Override
	public Bill mergeDetail(BillDetail detail) {
		EntityManager entityManager = entityManagerProvider.get();
		Boolean isNew = detail.getId() == null;
		detail.setValue(detail.getValue() != null ? detail.getValue().setScale(2, RoundingMode.HALF_EVEN) : null);
		detail.setUnits(detail.getUnits() != null ? detail.getUnits().setScale(2, RoundingMode.HALF_EVEN) : null);
		Bill bill;
		entityManager.getTransaction().begin();
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
		entityManager.getTransaction().commit();
		entityManager.clear();
		processResults(bill);
		entityManager.clear();
		bill = entityManager.find(Bill.class, detail.getBill().getId());
		return bill;
	}

	@Override
	public Bill removeDetail(BillDetail detail) {
		EntityManager entityManager = entityManagerProvider.get();
		Bill bill = detail.getBill();
		bill.getDetails().remove(detail);
		entityManager.getTransaction().begin();
		entityManager.remove(detail);
		entityManager.getTransaction().commit();
		entityManager.clear();
		bill = entityManager.find(Bill.class, detail.getBill().getId());
		processResults(bill);
		return bill;
	}
}
