package com.luckia.biller.core.services.bills.recalculation;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.Range;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.luckia.biller.core.model.Bill;
import com.luckia.biller.core.model.Company;
import com.luckia.biller.core.model.LegalEntity;
import com.luckia.biller.core.model.Store;
import com.luckia.biller.core.model.common.Message;
import com.luckia.biller.core.scheduler.tasks.BillRecalculationTask;
import com.luckia.biller.core.scheduler.tasks.BillTask;
import com.luckia.biller.core.services.bills.BillProcessor;

public class BillRecalculationService {

	private static final Logger LOG = LoggerFactory.getLogger(BillRecalculationService.class);

	@Inject
	private Provider<EntityManager> entityManagerProvider;
	@Inject
	private BillProcessor billProcessor;

	public Message<BillRecalculationInfo> prepare(Company company, Store store, Range<Date> range) {
		try {
			EntityManager entityManager = entityManagerProvider.get();
			CriteriaBuilder builder = entityManager.getCriteriaBuilder();
			CriteriaQuery<Bill> criteria = builder.createQuery(Bill.class);
			Root<Bill> root = criteria.from(Bill.class);
			List<Predicate> predicates = new ArrayList<Predicate>();
			predicates.add(builder.greaterThanOrEqualTo(root.<Date> get("dateFrom"), range.getMinimum()));
			predicates.add(builder.lessThanOrEqualTo(root.<Date> get("dateTo"), range.getMaximum()));
			if (store != null) {
				predicates.add(builder.equal(root.<LegalEntity> get("sender").<Long> get("id"), store.getId()));
			}
			if (company != null) {
				predicates.add(builder.equal(root.<LegalEntity> get("receiver").<Long> get("id"), company.getId()));
			}
			criteria.where(predicates.toArray(new Predicate[predicates.size()]));
			TypedQuery<Bill> query = entityManager.createQuery(criteria);
			List<Bill> bills = query.getResultList();
			BillRecalculationInfo payload = buildRecalculationInfo(company, store, range, bills);
			return new Message<BillRecalculationInfo>().withPayload(payload);
		} catch (Exception ex) {
			LOG.error("Error al recalcular la factura", ex);
			return new Message<BillRecalculationInfo>().withCode(Message.CODE_GENERIC_ERROR).addError("billRecalculation.prepare.error").addError(ex.getMessage());
		}
	}

	public Message<BillRecalculationInfo> execute(BillRecalculationInfo info) {
		try {
			List<Runnable> tasks = new ArrayList<>();
			// Primero recalculamos las facturas existentes
			if (info.getCurrentBills() != null) {
				for (BillRecalculationDetail detail : info.getCurrentBills()) {
					if (detail.getBillId() != null) {
						tasks.add(new BillRecalculationTask(detail.getBillId(), entityManagerProvider, billProcessor));
					}
				}
			}
			// En segundo lugar calculamos las facturas no existentes
			if (info.getNonExistingBills() != null) {
				Range<Date> range = Range.between(info.getFrom(), info.getTo());
				for (BillRecalculationDetail detail : info.getNonExistingBills()) {
					tasks.add(new BillTask(detail.getStoreId(), range, entityManagerProvider, billProcessor));
				}
			}
			if (!tasks.isEmpty()) {
				ExecutorService executorService = Executors.newFixedThreadPool(1);
				for (Runnable task : tasks) {
					executorService.execute(task);
				}
				executorService.shutdown();
				executorService.awaitTermination(5, TimeUnit.HOURS);
				LOG.debug("Finalizado el tratamiento de {} tareas", tasks.size());
			}
			return new Message<BillRecalculationInfo>(Message.CODE_SUCCESS, String.format("Recalculadas %s facturas", tasks.size()));
		} catch (Exception ex) {
			LOG.error("Error al recalcular la factura", ex);
			return new Message<BillRecalculationInfo>(Message.CODE_SUCCESS, "Error al recalcular la factura: " + ex.getMessage());
		}
	}

	private BillRecalculationInfo buildRecalculationInfo(Company company, Store store, Range<Date> range, List<Bill> bills) {
		BillRecalculationInfo result = new BillRecalculationInfo();
		result.setFrom(range.getMinimum());
		result.setTo(range.getMaximum());
		result.setStore(store);
		result.setCompany(company);
		result.setCurrentBills(new ArrayList<BillRecalculationDetail>());
		result.setNonExistingBills(new ArrayList<BillRecalculationDetail>());
		for (Bill bill : bills) {
			BillRecalculationDetail detail = new BillRecalculationDetail();
			detail.setBillId(bill.getId());
			detail.setStoreId(bill.getSender().getId());
			detail.setStoreName(bill.getSender().getName());
			result.getCurrentBills().add(detail);
		}
		if (store != null && !findByStore(store.getId(), bills)) {
			BillRecalculationDetail detail = new BillRecalculationDetail();
			detail.setStoreId(store.getId());
			detail.setStoreName(store.getName());
			result.getNonExistingBills().add(detail);
		}
		if (company != null) {
			EntityManager entityManager = entityManagerProvider.get();
			TypedQuery<Store> query = entityManager.createNamedQuery(Store.QUERY_SELECT_BY_COMPANY, Store.class);
			query.setParameter("company", company);
			for (Store i : query.getResultList()) {
				if (!findByStore(i.getId(), bills)) {
					BillRecalculationDetail detail = new BillRecalculationDetail();
					detail.setStoreId(i.getId());
					detail.setStoreName(i.getName());
					result.getNonExistingBills().add(detail);
				}
			}
		}
		return result;
	}

	private boolean findByStore(Long storeId, List<Bill> bills) {
		for (Bill bill : bills) {
			if (storeId.equals(bill.getSender().getId())) {
				return true;
			}
		}
		return false;
	}
}
