package com.luckia.biller.core.services.bills.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.apache.commons.lang3.Range;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.luckia.biller.core.jpa.EntityManagerProvider;
import com.luckia.biller.core.model.AppFile;
import com.luckia.biller.core.model.Bill;
import com.luckia.biller.core.model.CommonState;
import com.luckia.biller.core.model.Company;
import com.luckia.biller.core.model.CostCenter;
import com.luckia.biller.core.model.Liquidation;
import com.luckia.biller.core.model.LiquidationDetail;
import com.luckia.biller.core.model.Store;
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
	private EntityManagerProvider entityManagerProvider;
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.luckia.biller.core.services.bills.impl.LiquidationProcessor#process(com.luckia.biller.core.model.Company,
	 * org.apache.commons.lang3.Range)
	 */
	@Override
	public List<Liquidation> processBills(Company company, Range<Date> range) {
		EntityManager entityManager = entityManagerProvider.get();
		TypedQuery<Bill> query = entityManager.createNamedQuery("Bill.selectPendingByReceiverInRange", Bill.class);
		query.setParameter("receiver", company);
		query.setParameter("from", range.getMinimum());
		query.setParameter("to", range.getMaximum());
		List<Bill> bills = query.getResultList();
		Map<CostCenter, List<Bill>> costCenterMap = getCostCenterMapping(bills);
		if (!entityManager.getTransaction().isActive()) {
			entityManager.getTransaction().begin();
		}
		List<Liquidation> result = new ArrayList<Liquidation>();
		LOG.info("Centros de coste asociados a la liquidacion:");
		for (CostCenter costCenter : costCenterMap.keySet()) {
			LOG.info("Generando liquidacion asociada al centro de coste {}", costCenter.getName());
			List<Bill> billList = costCenterMap.get(costCenter);
			Liquidation liquidation = new Liquidation();
			liquidation.setId(UUID.randomUUID().toString());
			liquidation.setSender(company);
			liquidation.setReceiver(costCenter);
			liquidation.setBills(billList);
			liquidation.setDateFrom(range.getMinimum());
			liquidation.setDateTo(range.getMaximum());
			liquidation.setBillDate(range.getMaximum());
			liquidation.setModel(company.getBillingModel());

			BigDecimal totalAmount = BigDecimal.ZERO;
			for (Bill bill : liquidation.getBills()) {
				totalAmount = totalAmount.add(bill.getLiquidationAmount());
				bill.setLiquidation(liquidation);
				entityManager.merge(bill);
			}
			liquidation.setAmount(totalAmount);
			LOG.debug("Resultado de la liquidacion: {}", totalAmount);
			entityManager.merge(liquidation);
			auditService.processCreated(liquidation);
			result.add(liquidation);
			stateMachineService.createTransition(liquidation, CommonState.Draft.name());
		}
		entityManager.getTransaction().commit();
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.luckia.biller.core.services.bills.LiquidationProcessor#confirm(com.luckia.biller.core.model.Liquidation)
	 */
	@Override
	public void confirm(Liquidation liquidation) {
		try {
			EntityManager entityManager = entityManagerProvider.get();
			entityManager.getTransaction().begin();
			stateMachineService.createTransition(liquidation, CommonState.Confirmed.name());
			File tempFile = File.createTempFile("tmp-bill-", ".pdf");
			FileOutputStream out = new FileOutputStream(tempFile);
			pdfLiquidationGenerator.generate(liquidation, out);
			out.close();
			FileInputStream in = new FileInputStream(tempFile);
			String name = String.format("bill-%s.pdf", liquidation.getCode());
			AppFile pdfFile = fileService.save(name, "application/pdf", in);
			liquidation.setPdfFile(pdfFile);
			entityManager.merge(liquidation);
			liquidationCodeGenerator.generateCode(liquidation);
			entityManager.getTransaction().commit();
		} catch (Exception ex) {
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
			entityManager.merge(detail);
			liquidation = current.getLiquidation();
		}
		entityManager.getTransaction().commit();
		entityManager.clear();
		// TODO ACTUALIZAR RESULTADO DE LA LIQUIDACION
		liquidation = entityManager.find(Liquidation.class, liquidation.getId());
		return liquidation;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.luckia.biller.core.services.bills.LiquidationProcessor#removeDetail(com.luckia.biller.core.model.LiquidationDetail)
	 */
	@Override
	public Liquidation removeDetail(LiquidationDetail detail) {
		throw new RuntimeException("Not implemented");
	}

	/**
	 * Desglosa las facturas por centro de coste asociadas a una empresa.
	 * 
	 * @param bills
	 * @return
	 */
	private Map<CostCenter, List<Bill>> getCostCenterMapping(List<Bill> bills) {
		Map<CostCenter, List<Bill>> costCenterMap = new LinkedHashMap<CostCenter, List<Bill>>();
		for (Bill bill : bills) {
			if (!CommonState.Empty.equals(bill.getCurrentState().getStateDefinition().getId())) {
				Store store = bill.getSender(Store.class);
				CostCenter costCenter = store.getCostCenter();
				if (costCenter == null) {
					LOG.warn("El local {} de la factura {} carece de centro de coste. No se puede realizar la liquidacion", store, bill);
				} else {
					if (!costCenterMap.containsKey(store.getCostCenter())) {
						costCenterMap.put(costCenter, new ArrayList<Bill>());
					}
					costCenterMap.get(costCenter).add(bill);
				}
			}
		}
		return costCenterMap;
	}

}
