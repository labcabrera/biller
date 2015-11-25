package com.luckia.biller.core.jpa;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Provider;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.junit.Ignore;
import org.junit.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.persist.PersistService;
import com.luckia.biller.core.BillerModule;
import com.luckia.biller.core.model.CommonState;
import com.luckia.biller.core.model.Liquidation;
import com.luckia.biller.core.services.bills.LiquidationProcessor;

@Ignore
public class TestConcurrent {

	@Test
	public void test() throws InterruptedException {
		Injector injector = Guice.createInjector(new BillerModule());
		injector.getInstance(PersistService.class).start();
		Provider<EntityManager> entityManagerProvider = injector.getProvider(EntityManager.class);
		LiquidationProcessor liquidationProcessor = injector.getInstance(LiquidationProcessor.class);

		EntityManager entityManager = entityManagerProvider.get();
		String qlString = "select e from Liquidation e where e.currentState.stateDefinition.id = :state";
		TypedQuery<Liquidation> query = entityManager.createQuery(qlString, Liquidation.class);
		query.setParameter("state", CommonState.Sent.name());
		query.setMaxResults(1);
		Liquidation liquidation = query.getSingleResult();

		List<Thread> threads = new ArrayList<Thread>();
		threads.add(new Thread(new InternalRunnable(liquidation.getId(), liquidationProcessor)));
		threads.add(new Thread(new InternalRunnable(liquidation.getId(), liquidationProcessor)));

		for (Thread thread : threads) {
			thread.start();
		}
		for (Thread thread : threads) {
			thread.join();
		}
	}

	private static class InternalRunnable implements Runnable {

		private final LiquidationProcessor liquidationProcessor;
		private final String liquidationId;

		public InternalRunnable(String liquidationId, LiquidationProcessor liquidationProcessor) {
			this.liquidationId = liquidationId;
			this.liquidationProcessor = liquidationProcessor;
		}

		@Override
		public void run() {
			liquidationProcessor.recalculate(liquidationId);
		}

	}
}
