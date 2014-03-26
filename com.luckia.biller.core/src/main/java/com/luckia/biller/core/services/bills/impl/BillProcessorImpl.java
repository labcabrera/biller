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

import com.luckia.biller.core.jpa.EntityManagerProvider;
import com.luckia.biller.core.model.AppFile;
import com.luckia.biller.core.model.Bill;
import com.luckia.biller.core.model.BillDetail;
import com.luckia.biller.core.model.BillState;
import com.luckia.biller.core.model.BillType;
import com.luckia.biller.core.model.LiquidationDetail;
import com.luckia.biller.core.model.Store;
import com.luckia.biller.core.services.AuditService;
import com.luckia.biller.core.services.FileService;
import com.luckia.biller.core.services.SettingsService;
import com.luckia.biller.core.services.StateMachineService;
import com.luckia.biller.core.services.bills.BillCodeGenerator;
import com.luckia.biller.core.services.bills.BillDetailProcessor;
import com.luckia.biller.core.services.bills.BillProcessor;
import com.luckia.biller.core.services.pdf.PdfBillGenerator;

public class BillProcessorImpl implements BillProcessor {

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
	private PdfBillGenerator pdfBillGenerator;
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
		stateMachineService.createTransition(bill, BillState.BillInitial.name());
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
		if (bill.getDetails().isEmpty()) {
			stateMachineService.createTransition(bill, BillState.BillEmpty.name());
		} else {
			EntityManager entityManager = entityManagerProvider.get();
			entityManager.getTransaction().begin();
			for (BillDetail detail : bill.getDetails()) {
				entityManager.merge(detail);
			}
			entityManager.merge(bill);
			stateMachineService.createTransition(bill, BillState.BillDraft.name());
			entityManager.getTransaction().commit();
		}
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
		BigDecimal vatAmount = netAmount.multiply(vatPercent).divide(new BigDecimal("100.00"), 2, RoundingMode.HALF_EVEN);
		BigDecimal amount = netAmount.add(vatAmount);
		bill.setNetAmount(netAmount);
		bill.setAmount(amount);
		bill.setVatPercent(vatPercent);
		bill.setVatAmount(vatAmount);

		BigDecimal liquidationAmount = BigDecimal.ZERO;
		for (LiquidationDetail detail : bill.getLiquidationDetails()) {
			liquidationAmount = liquidationAmount.add(detail.getValue());
		}
		bill.setLiquidationAmount(liquidationAmount);

		EntityManager entityManager = entityManagerProvider.get();
		entityManager.getTransaction().begin();
		entityManager.merge(bill);
		entityManager.getTransaction().commit();
	}

	@Override
	public void confirmBill(Bill bill) {
		try {
			EntityManager entityManager = entityManagerProvider.get();
			entityManager.getTransaction().begin();
			stateMachineService.createTransition(bill, BillState.BillConfirmed.name());
			File tempFile = File.createTempFile("tmp-bill-", ".pdf");
			FileOutputStream out = new FileOutputStream(tempFile);
			pdfBillGenerator.generate(bill, out);
			out.close();
			FileInputStream in = new FileInputStream(tempFile);
			String name = String.format("bill-%s.pdf", bill.getCode());
			AppFile pdfFile = fileService.save(name, "application/pdf", in);
			bill.setPdfFile(pdfFile);
			entityManager.merge(bill);
			billCodeGenerator.generateCode(bill);
			entityManager.getTransaction().commit();
		} catch (Exception ex) {
			throw new RuntimeException("Error al confirmar la factura", ex);
		}
	}

	@Override
	public Bill rectifyBill(Bill bill) {
		EntityManager entityManager = entityManagerProvider.get();
		entityManager.getTransaction().begin();
		stateMachineService.createTransition(bill, BillState.BillRectified.name());
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
		rectified.setLiquidationDetails(new ArrayList<LiquidationDetail>());
		rectified.setParent(bill);
		entityManager.persist(rectified);
		for (BillDetail detail : bill.getDetails()) {
			BillDetail copy = detail.clone();
			copy.setBill(rectified);
			copy.setId(UUID.randomUUID().toString());
			rectified.getDetails().add(copy);
			entityManager.persist(rectified);
		}
		entityManager.merge(rectified);
		stateMachineService.createTransition(rectified, BillState.BillDraft.name());
		entityManager.getTransaction().commit();
		processResults(bill);
		return rectified;
	}
}
