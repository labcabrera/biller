package com.luckia.biller.deploy;

import java.util.Date;
import java.util.List;

import javax.inject.Provider;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.persist.PersistService;
import com.luckia.biller.core.LuckiaCoreModule;
import com.luckia.biller.core.model.Bill;
import com.luckia.biller.core.scheduler.tasks.BillRecalculationTask;
import com.luckia.biller.core.services.bills.BillProcessor;

public class Patch20140605A extends PatchSupport implements Runnable {

	private static final Logger LOG = LoggerFactory.getLogger(Patch20140605A.class);

	public static void main(String[] args) {
		new Patch20140605A().run();
	}

	public void run() {
		if (!confirm()) {
			System.out.println("Application aborted");
			return;
		}
		try {
			LOG.info("Ejecutando patch");
			Injector injector = Guice.createInjector(new LuckiaCoreModule());
			injector.getInstance(PersistService.class).start();
			Provider<EntityManager> entityManagerProvider = injector.getProvider(EntityManager.class);
			EntityManager entityManager = entityManagerProvider.get();
			BillProcessor billProcessor = injector.getInstance(BillProcessor.class);

			Date from = new DateTime(2014, 5, 1, 0, 0, 0, 0).toDate();
			Date to = new DateTime(2014, 5, 31, 0, 0, 0, 0).toDate();

			String qlString = "select b from Bill b where b.dateFrom >= :from and b.dateTo <= :to";
			TypedQuery<Bill> query = entityManager.createQuery(qlString, Bill.class);
			query.setParameter("from", from);
			query.setParameter("to", to);
			List<Bill> list = query.getResultList();
			LOG.info("Encontradas {} facturas", list.size());

			for (Bill bill : list) {
				try {
					String billId = bill.getId();
					BillRecalculationTask task = new BillRecalculationTask(billId, entityManagerProvider, billProcessor);
					task.run();
				} catch (Exception ex) {
					LOG.error("Error en la factura " + bill.getSender().getName(), ex);
				}
			}

		} catch (Exception ex) {
			LOG.error(ex.getMessage(), ex);
		}
	}
}
