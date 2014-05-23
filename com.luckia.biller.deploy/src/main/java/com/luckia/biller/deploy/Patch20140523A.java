package com.luckia.biller.deploy;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.apache.commons.lang3.Range;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.luckia.biller.core.LuckiaCoreModule;
import com.luckia.biller.core.jpa.EntityManagerProvider;
import com.luckia.biller.core.model.Company;
import com.luckia.biller.core.model.Liquidation;
import com.luckia.biller.core.scheduler.tasks.LiquidationRecalculationTask;
import com.luckia.biller.core.services.bills.BillProcessor;
import com.luckia.biller.core.services.bills.LiquidationProcessor;

public class Patch20140523A {

	private static final Logger LOG = LoggerFactory.getLogger(Patch20140523A.class);

	public static void main(String[] args) {
		LOG.info("Ejecutando patch");
		Injector injector = Guice.createInjector(new LuckiaCoreModule());
		EntityManagerProvider entityManagerProvider = injector.getInstance(EntityManagerProvider.class);
		LiquidationProcessor liquidationProcessor = injector.getInstance(LiquidationProcessor.class);
		EntityManager entityManager = entityManagerProvider.get();

		Date from = new DateTime(2014, 2, 1, 0, 0, 0, 0).toDate();
		Date to = new DateTime(2014, 3, 31, 0, 0, 0, 0).toDate();
		String companyName = "Replay, S.L. (bares)";

		String qlStringCompany = "select e from Company e where e.name = :name";
		String qlStringLiquidation = "select e from Liquidation e where e.sender = :company and e.billDate >= :from and e.billDate <= :to";

		Company company = entityManager.createQuery(qlStringCompany, Company.class).setParameter("name", companyName).getSingleResult();
		TypedQuery<Liquidation> query = entityManager.createQuery(qlStringLiquidation, Liquidation.class);
		query.setParameter("from", from);
		query.setParameter("to", to);
		query.setParameter("company", company);

		List<Liquidation> liquidations = query.getResultList();
		LOG.debug("Encontradas {} liquidaciones");
		for (Liquidation liquidation : liquidations) {
			String liquidationId = liquidation.getId();
			LiquidationRecalculationTask task = new LiquidationRecalculationTask(liquidationId, entityManagerProvider, liquidationProcessor);
			task.run();
		}
	}
}
