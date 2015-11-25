package com.luckia.biller.core.jpa;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.eclipse.persistence.config.QueryHints;
import org.junit.Ignore;
import org.junit.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.persist.PersistService;
import com.luckia.biller.core.BillerModule;
import com.luckia.biller.core.model.Liquidation;
import com.luckia.biller.core.serialization.Serializer;

@Ignore
public class TestFetchGroup {

	@Test
	public void test() {
		try {
			Injector injector = Guice.createInjector(new BillerModule());
			injector.getInstance(PersistService.class).start();
			Serializer serializer = injector.getInstance(Serializer.class);
			EntityManager entityManager = injector.getProvider(EntityManager.class).get();
			Long t0 = System.currentTimeMillis();
			TypedQuery<Liquidation> query = entityManager.createQuery("select e from Liquidation e", Liquidation.class);
			query.setMaxResults(10);
			query.setHint(QueryHints.FETCH_GROUP_NAME, "list");
			List<Liquidation> liquidations = query.getResultList();
			for (Liquidation liquidation : liquidations) {
				System.out.println(serializer.toJson(liquidation));
				System.out.println("--------------------------------------------");
			}
			System.out.println("Time: " + (System.currentTimeMillis() - t0) + " ms");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

}
