package com.luckia.biller.core.services.pdf;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.junit.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.persist.PersistService;
import com.luckia.biller.core.BillerModule;
import com.luckia.biller.core.model.Liquidation;

public class PDFLiquidationGeneratorTest {

	@Test
	public void test() throws FileNotFoundException {
		Injector injector = Guice.createInjector(new BillerModule());
		injector.getInstance(PersistService.class).start();
		EntityManager entityManager = injector.getProvider(EntityManager.class).get();
		TypedQuery<Liquidation> query = entityManager.createQuery("select e from Liquidation e order by e.code desc", Liquidation.class);
		Liquidation liquidation = query.setMaxResults(1).getSingleResult();
		FileOutputStream out = new FileOutputStream("./target/test-liquidation.pdf");
		PDFLiquidationGenerator generator = injector.getInstance(PDFLiquidationGenerator.class);
		generator.generate(liquidation, out);
	}
}
