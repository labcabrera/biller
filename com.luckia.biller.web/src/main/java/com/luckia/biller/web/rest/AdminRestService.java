package com.luckia.biller.web.rest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.commons.lang3.Range;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.luckia.biller.core.ClearCache;
import com.luckia.biller.core.jpa.EntityManagerProvider;
import com.luckia.biller.core.model.Bill;
import com.luckia.biller.core.model.CommonState;
import com.luckia.biller.core.model.Company;
import com.luckia.biller.core.model.Store;
import com.luckia.biller.core.model.common.Message;
import com.luckia.biller.core.scheduler.tasks.BillRecalculationTask;
import com.luckia.biller.core.scheduler.tasks.BillTask;
import com.luckia.biller.core.scheduler.tasks.LiquidationTask;
import com.luckia.biller.core.services.bills.BillProcessor;
import com.luckia.biller.core.services.bills.LiquidationProcessor;

@Path("rest/admin")
public class AdminRestService {

	private static final Logger LOG = LoggerFactory.getLogger(AdminRestService.class);
	private static final int RECALCULATE_BILLS_THREAD_COUNT = 10;

	@Inject
	private EntityManagerProvider entityManagerProvider;
	@Inject
	private BillProcessor billProcessor;
	@Inject
	private LiquidationProcessor liquidationProcessor;

	// TODO filtrar por el estado
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/recalculate/bills/{year}/{month}")
	@ClearCache
	public Message<String> find(@PathParam("year") Integer year, @PathParam("month") Integer month) {
		LOG.info("Recalculando facturacion de {}/{}", year, month);
		DateTime from = new DateTime(year, month, 1, 0, 0, 0, 0);
		DateTime to = from.dayOfMonth().withMaximumValue();
		EntityManager entityManager = entityManagerProvider.get();
		String qlString = "select b.id from Bill b where b.billDate >= :from and b.billDate <= :to and b.currentState.stateDefinition.id in :states";
		TypedQuery<String> query = entityManager.createQuery(qlString, String.class);
		query.setParameter("from", from.toDate());
		query.setParameter("to", to.toDate());
		query.setParameter("states", Arrays.asList(CommonState.Initial, CommonState.Draft, CommonState.Empty));
		List<String> billIds = query.getResultList();

		ExecutorService executorService = Executors.newFixedThreadPool(RECALCULATE_BILLS_THREAD_COUNT);
		Long t0 = System.currentTimeMillis();
		for (String billId : billIds) {
			BillRecalculationTask task = new BillRecalculationTask(billId, entityManagerProvider, billProcessor);
			executorService.submit(task);
		}
		LOG.debug("Esperando a la finalizacion de {} tareas (hilos: {})", billIds.size(), RECALCULATE_BILLS_THREAD_COUNT);
		executorService.shutdown();
		try {
			executorService.awaitTermination(5, TimeUnit.HOURS);
			LOG.debug("Finalizadas {} tareas en {} ms", billIds.size(), (System.currentTimeMillis() - t0));
		} catch (InterruptedException ex) {
			LOG.error("Error durante la ejecucion de las tareas", ex);
		}

		// Paso 2 buscar las nuevas empresas que hayan sido creadas que carecen de facturas
		LOG.info("Buscando nuevos establecimientos para los que hay que generar las facturas");
		TypedQuery<Store> storeQuery = entityManager.createQuery("select s from Store s order by s.name", Store.class);
		TypedQuery<Bill> queryBills = entityManager.createQuery("select b from Bill b where b.sender = :store and b.billDate >= :from and b.billDate <= :to", Bill.class);
		List<Store> stores = storeQuery.getResultList();
		for (Store store : stores) {
			queryBills.setParameter("store", store);
			queryBills.setParameter("from", from.toDate());
			queryBills.setParameter("to", to.toDate());
			if (queryBills.getResultList().isEmpty()) {
				LOG.debug("Detectado establecimiento sin facturas: " + store.getName());
				// TODO
			}
		}
		return new Message<String>(Message.CODE_SUCCESS, String.format("Recalculadas %s facturas", billIds.size()));
	}

	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/patch/replay/2014/21")
	@ClearCache
	public Message<String> execute() {
		EntityManager entityManager = entityManagerProvider.get();
		TypedQuery<Company> queryCompanies = entityManager.createQuery("select c from Company c where c.name like :name1 or c.name like :name2", Company.class);
		// List<Company> companies = queryCompanies.setParameter("name1", "%Replay%").setParameter("name2", "%Videomani%").getResultList();
		List<Company> companies = queryCompanies.setParameter("name2", "%Videomani%").getResultList();
		LOG.debug("Encontradas {} empresas", companies.size());

		List<Range<Date>> ranges = new ArrayList<>();
		ranges.add(Range.between(new DateTime(2014, 1, 1, 0, 0, 0, 0).toDate(), new DateTime(2014, 1, 31, 0, 0, 0, 0).toDate()));
		// ranges.add(Range.between(new DateTime(2014, 2, 1, 0, 0, 0, 0).toDate(), new DateTime(2014, 2, 28, 0, 0, 0, 0).toDate()));
		// ranges.add(Range.between(new DateTime(2014, 3, 1, 0, 0, 0, 0).toDate(), new DateTime(2014, 3, 31, 0, 0, 0, 0).toDate()));
		// ranges.add(Range.between(new DateTime(2014, 4, 1, 0, 0, 0, 0).toDate(), new DateTime(2014, 4, 30, 0, 0, 0, 0).toDate()));

		LOG.debug("------------------------- regenerando facturas ------------------------");
		for (Company company : companies) {
			LOG.info("Recalculando liquidaciones de " + company.getName());
			// TypedQuery<Liquidation> queryLiquidations = entityManager.createQuery("select e from Liquidation e where e.sender = :sender",
			// Liquidation.class);
			// List<Liquidation> liquidations = queryLiquidations.setParameter("sender", company).getResultList();
			// LOG.debug("Encontradas {} liquidaciones", liquidations.size());
			// // Step 1: borrar liquidaciones
			// for (Liquidation liquidation : liquidations) {
			// // TODO borrar liquidacion
			// }

			// Step 2: recalculamos las facturas
			TypedQuery<Long> query = entityManager.createQuery("select s.id from Store s where s.parent = :company", Long.class);
			query.setParameter("company", company);
			List<Long> storeIds = query.getResultList();
			LOG.debug("Encontrados {} establecimientos de {}", storeIds.size(), company.getName());

			for (Range<Date> range : ranges) {
				LOG.debug("------------------------- rango {} {} ------------------------", range.getMinimum(), range.getMaximum());
				// Procesamos de forma asincrona las facturas
				for (Long storeId : storeIds) {
					BillTask task = new BillTask(storeId, range, entityManagerProvider, billProcessor);
					task.run();
				}
			}
		}

		// LOG.debug("------------------------- regenerando liquidaciones ------------------------");
		// try {
		// Thread.sleep(30000);
		// } catch (InterruptedException ex) {
		// }
		//
		// for (Company company : companies) {
		// // Step 3 : regeneramos las liquidaciones
		// for (Range<Date> range : ranges) {
		// LOG.debug("------------------------- rango {} {} ------------------------", range.getMinimum(), range.getMaximum());
		// LiquidationTask task = new LiquidationTask(company.getId(), range, entityManagerProvider, liquidationProcessor);
		// task.run();
		// }
		// }

		return new Message<String>(Message.CODE_SUCCESS, "Ejecutado patch");
	}
}
