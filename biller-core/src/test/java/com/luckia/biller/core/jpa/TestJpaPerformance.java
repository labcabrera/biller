package com.luckia.biller.core.jpa;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.junit.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.persist.PersistService;
import com.luckia.biller.core.BillerModule;
import com.luckia.biller.core.model.Liquidation;
import com.luckia.biller.core.serialization.Serializer;

public class TestJpaPerformance {

	@Test
	public void test() {
		try {
			Injector injector = Guice.createInjector(new BillerModule());
			injector.getInstance(PersistService.class).start();
			Serializer serializer = injector.getInstance(Serializer.class);
			EntityManager entityManager = injector.getProvider(EntityManager.class).get();
			Long t0 = System.currentTimeMillis();
			TypedQuery<Liquidation> query = entityManager.createQuery("select e from Liquidation e where e.id = :id", Liquidation.class);
			query.setParameter("id", "00039de9-0d0d-45ea-a0df-20f383befcf4");
			query.setMaxResults(10);
			
			//query.setHint(QueryHints.FE, arg1)
			
			List<Liquidation> liquidations = query.getResultList();
			Long timeQuery = System.currentTimeMillis() - t0;
			t0 = System.currentTimeMillis();
			for (Liquidation liquidation : liquidations) {
				System.out.println(serializer.toJson(liquidation));
				System.out.println("--------------------------------------------");
			}
			System.out.println("Time query: " + timeQuery + " ms");
			System.out.println("Time serialization: " + (System.currentTimeMillis() - t0) + " ms");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

}
