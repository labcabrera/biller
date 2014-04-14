package com.luckia.biller.core.services;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.junit.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.luckia.biller.core.LuckiaCoreModule;
import com.luckia.biller.core.jpa.EntityManagerProvider;
import com.luckia.biller.core.model.Liquidation;

public class ZipFileServiceTest {

	@Test
	public void test() throws FileNotFoundException {
		Injector injector = Guice.createInjector(new LuckiaCoreModule());
		EntityManager entityManager = injector.getInstance(EntityManagerProvider.class).get();

		TypedQuery<Liquidation> query = entityManager.createQuery("select e from Liquidation e where e.pdfFile is not null", Liquidation.class).setMaxResults(1);
		List<Liquidation> list = query.getResultList();
		if (list.isEmpty()) {
			System.out.println("No se encuentran liquidaciones validas para realizar el test");
		} else {
			FileOutputStream out = new FileOutputStream(new File("./target/test-liquidation.zip"));
			ZipFileService zipFileService = injector.getInstance(ZipFileService.class);
			zipFileService.generate(list.iterator().next(), out);
		}
	}
}
