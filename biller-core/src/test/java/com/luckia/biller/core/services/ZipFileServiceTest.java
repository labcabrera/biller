package com.luckia.biller.core.services;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.junit.Ignore;
import org.junit.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.persist.PersistService;
import com.luckia.biller.core.BillerModule;
import com.luckia.biller.core.model.Liquidation;

@Ignore
public class ZipFileServiceTest {

	@Test
	public void test() throws FileNotFoundException {
		Injector injector = Guice.createInjector(new BillerModule());
		injector.getInstance(PersistService.class).start();
		EntityManager entityManager = injector.getProvider(EntityManager.class).get();

		TypedQuery<Liquidation> query = entityManager.createQuery("select e from Liquidation e where e.pdfFile is not null", Liquidation.class).setMaxResults(1);
		List<Liquidation> list = query.getResultList();
		if (list.isEmpty()) {
			System.out.println("No se encuentran liquidaciones validas para realizar el test");
		} else {
			FileOutputStream out = new FileOutputStream(new File("./build/test-liquidation.zip"));
			ZipFileService zipFileService = injector.getInstance(ZipFileService.class);
			zipFileService.generate(list.iterator().next(), out);
		}
	}
}
