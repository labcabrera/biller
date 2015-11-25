package com.luckia.biller.core.services.pdf;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import javax.persistence.EntityManager;

import org.junit.Ignore;
import org.junit.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.persist.PersistService;
import com.luckia.biller.core.BillerModule;
import com.luckia.biller.core.model.Bill;

@Ignore
public class PDFBillGeneratorTest {

	@Test
	public void test() throws FileNotFoundException {
		Injector injector = Guice.createInjector(new BillerModule());
		injector.getInstance(PersistService.class).start();
		EntityManager entityManager = injector.getProvider(EntityManager.class).get();
		Bill bill = entityManager.createQuery("select b from Bill b order by b.code desc", Bill.class).setMaxResults(1).getSingleResult();
		FileOutputStream out = new FileOutputStream("./target/test-bill.pdf");
		PDFBillGenerator generator = injector.getInstance(PDFBillGenerator.class);
		generator.generate(bill, out);
	}
}
