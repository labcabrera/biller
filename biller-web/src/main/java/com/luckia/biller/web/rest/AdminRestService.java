package com.luckia.biller.web.rest;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.commons.lang3.Range;
import org.apache.commons.lang3.Validate;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.luckia.biller.core.model.Bill;
import com.luckia.biller.core.model.Company;
import com.luckia.biller.core.model.LegalEntity;
import com.luckia.biller.core.model.Liquidation;
import com.luckia.biller.core.model.Store;
import com.luckia.biller.core.model.common.Message;
import com.luckia.biller.core.scheduler.tasks.LiquidationRecalculationTask;
import com.luckia.biller.core.scheduler.tasks.LiquidationTask;
import com.luckia.biller.core.services.bills.BillProcessor;
import com.luckia.biller.core.services.bills.LiquidationProcessor;

@Path("/admin")
public class AdminRestService {

	private static final Logger LOG = LoggerFactory.getLogger(AdminRestService.class);
	private static final int RECALCULATE_BILLS_THREAD_COUNT = 10;
	private static final boolean DISABLE_RECALCULATE = true;

	@Inject
	private Provider<EntityManager> entityManagerProvider;
	@Inject
	private BillProcessor billProcessor;
	@Inject
	private LiquidationProcessor liquidationProcessor;

	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/recalculate/bills/{year}/{month}")
	public Message<String> recalculateBills(@PathParam("year") Integer year, @PathParam("month") Integer month) {
		return recalculateBill(null, year, month);
	}

	/**
	 * Genera una nueva factura o la regenera si ya existe a partir del establecimiento y la fecha.
	 * 
	 * @param storeId
	 * @param year
	 * @param month
	 * @return
	 */
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/recalculate/bill/{storeId}/{year}/{month}")
	public Message<String> recalculateBill(@PathParam("storeId") Long storeId, @PathParam("year") Integer year, @PathParam("month") Integer month) {
		try {
			Range<Date> range = getEffectiveRange(year, month);
			EntityManager entityManager = entityManagerProvider.get();
			CriteriaBuilder builder = entityManager.getCriteriaBuilder();
			CriteriaQuery<Bill> criteria = builder.createQuery(Bill.class);
			Root<Bill> root = criteria.from(Bill.class);
			List<Predicate> predicates = new ArrayList<Predicate>();
			predicates.add(builder.greaterThanOrEqualTo(root.<Date> get("dateFrom"), range.getMinimum()));
			predicates.add(builder.lessThanOrEqualTo(root.<Date> get("dateTo"), range.getMaximum()));
			if (storeId != null) {
				predicates.add(builder.equal(root.<LegalEntity> get("sender").<Long> get("id"), storeId));
			}
			criteria.where(predicates.toArray(new Predicate[predicates.size()]));
			TypedQuery<Bill> query = entityManager.createQuery(criteria);
			List<Bill> bills = query.getResultList();
			LOG.debug("Se van a recalcular {} facturas", bills.size());

			// Store store = entityManager.find(Store.class, storeId);
			// Validate.notNull(store, "No se encuentra el establecimiento");
			//
			// TypedQuery<Bill> query = entityManager.createNamedQuery("Bill.selectByStoreInRange", Bill.class);
			// query.setParameter("sender", store);
			// query.setParameter("from", range.getMinimum());
			// query.setParameter("to", range.getMaximum());
			// List<Bill> bills = query.getResultList();
			// String message;
			// Runnable task = null;
			// if (bills.isEmpty()) {
			// task = new BillTask(storeId, range, entityManagerProvider, billProcessor);
			// message = "Factura generada";
			// } else if (bills.size() > 1) {
			// message = "Se han encontrado varias facturas. Utilice la opcion de recalcular desde la vista de facturas.";
			// } else {
			// String billId = bills.iterator().next().getId();
			// task = new BillRecalculationTask(billId, entityManagerProvider, billProcessor);
			// message = "Factura recalculada";
			// }
			// if (task != null) {
			// new Thread(task).start();
			// }
			return new Message<String>(Message.CODE_SUCCESS, String.format("Recalculadas %s facturas", bills.size()));
		} catch (Exception ex) {
			LOG.error("Error al recalcular la factura", ex);
			return new Message<String>(Message.CODE_SUCCESS, "Error al recalcular la factura: " + ex.getMessage());
		}
	}

	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/recalculate/liquidations/{year}/{month}")
	public Message<String> recalculateLiquidation(@PathParam("year") Integer year, @PathParam("month") Integer month) {
		return recalculateLiquidation(null, year, month);
	}

	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/recalculate/liquidation/{companyId}/{year}/{month}")
	public Message<String> recalculateLiquidation(@PathParam("companyId") Long companyId, @PathParam("year") Integer year, @PathParam("month") Integer month) {
		try {
			Range<Date> range = getEffectiveRange(year, month);
			EntityManager entityManager = entityManagerProvider.get();
			CriteriaBuilder builder = entityManager.getCriteriaBuilder();
			CriteriaQuery<Liquidation> criteria = builder.createQuery(Liquidation.class);
			Root<Liquidation> root = criteria.from(Liquidation.class);
			List<Predicate> predicates = new ArrayList<Predicate>();
			predicates.add(builder.greaterThanOrEqualTo(root.<Date> get("dateFrom"), range.getMinimum()));
			predicates.add(builder.lessThanOrEqualTo(root.<Date> get("dateTo"), range.getMaximum()));
			if (companyId != null) {
				predicates.add(builder.equal(root.<LegalEntity> get("sender").<Long> get("id"), companyId));
			}
			criteria.where(predicates.toArray(new Predicate[predicates.size()]));
			TypedQuery<Liquidation> query = entityManager.createQuery(criteria);
			List<Liquidation> liquidations = query.getResultList();
			LOG.debug("Se van a recalcular {} facturas", liquidations.size());

			String message;
			// Runnable task = null;
			// if (liquidations.isEmpty()) {
			// task = new LiquidationTask(companyId, range, entityManagerProvider, liquidationProcessor);
			message = "Liquidación generada";
			// } else if (liquidations.size() > 1) {
			// message = "Se han encontrado varias facturas. Utilice la opcion de recalcular desde la vista de facturas.";
			// } else {
			// String liquidationId = liquidations.iterator().next().getId();
			// task = new LiquidationRecalculationTask(liquidationId, entityManagerProvider, liquidationProcessor);
			// message = "Liquidación recalculada";
			// }
			// if (task != null) {
			// new Thread(task).start();
			// }
			return new Message<String>(Message.CODE_SUCCESS, message);
		} catch (Exception ex) {
			LOG.error("Error al recalcular la factura", ex);
			return new Message<String>(Message.CODE_SUCCESS, "Error al recalcular la factura: " + ex.getMessage());
		}
	}

	public Message<String> calculateNewStores(@PathParam("year") Integer year, @PathParam("month") Integer month) {
		try {
			Range<Date> range = getEffectiveRange(year, month);
			EntityManager entityManager = entityManagerProvider.get();
			LOG.info("Buscando nuevos establecimientos para los que hay que generar las facturas");
			TypedQuery<Store> storeQuery = entityManager.createQuery("select s from Store s order by s.name", Store.class);
			TypedQuery<Bill> queryBills = entityManager.createQuery("select b from Bill b where b.sender = :store and b.billDate >= :from and b.billDate <= :to", Bill.class);
			List<Store> stores = storeQuery.getResultList();
			List<Store> targets = new ArrayList<>();
			for (Store store : stores) {
				queryBills.setParameter("store", store);
				queryBills.setParameter("from", range.getMinimum());
				queryBills.setParameter("to", range.getMaximum());
				if (queryBills.getResultList().isEmpty()) {
					LOG.debug("Detectado establecimiento sin facturas: " + store.getName());
					targets.add(store);
				}
			}
			// TODO Procesar esos establecimientos
			return new Message<String>(Message.CODE_SUCCESS, String.format("Encontrados %s establecimientos sin facturacion", targets.size()));
		} catch (Exception ex) {
			LOG.error("Error al recalcular la factura", ex);
			return new Message<String>(Message.CODE_SUCCESS, "Error al recalcular la factura: " + ex.getMessage());
		}
	}

	private Range<Date> getEffectiveRange(Integer year, Integer month) {
		DateTime from = new DateTime(year, month, 1, 0, 0, 0, 0);
		DateTime to = from.dayOfMonth().withMaximumValue();
		return Range.between(from.toDate(), to.toDate());
	}
}
