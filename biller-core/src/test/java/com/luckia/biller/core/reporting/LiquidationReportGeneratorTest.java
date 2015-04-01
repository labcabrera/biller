package com.luckia.biller.core.reporting;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Arrays;
import java.util.Date;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.joda.time.DateTime;
import org.junit.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.persist.PersistService;
import com.luckia.biller.core.LuckiaCoreModule;
import com.luckia.biller.core.model.LegalEntity;

public class LiquidationReportGeneratorTest {

	@Test
	public void test() throws FileNotFoundException {
		Injector injector = Guice.createInjector(new LuckiaCoreModule());
		injector.getInstance(PersistService.class).start();
		LiquidationReportGenerator generator = injector.getInstance(LiquidationReportGenerator.class);
		EntityManager entityManager = injector.getProvider(EntityManager.class).get();
		LegalEntity legalEntity01 = getLegalEntity(entityManager, "Videomani Siglo XXI, S.L");
		LegalEntity legalEntity02 = getLegalEntity(entityManager, "Replay S.L.");
		LegalEntity legalEntity03 = getLegalEntity(entityManager, "IMPULSORA TURISTICA ALPAMAN, S.A.");
		Date from = new DateTime(2014, 1, 1, 0, 0, 0, 0).toDate();
		Date to = new DateTime(2014, 6, 30, 0, 0, 0, 0).toDate();
		FileOutputStream out = new FileOutputStream("./target/liquidation.xls");
		generator.generate(from, to, Arrays.asList(legalEntity01, legalEntity02, legalEntity03), out);
	}

	private LegalEntity getLegalEntity(EntityManager entityManager, String name) {
		TypedQuery<LegalEntity> query = entityManager.createNamedQuery("LegalEntity.selectByName", LegalEntity.class);
		query.setParameter("name", name);
		return query.getSingleResult();
	}
}
