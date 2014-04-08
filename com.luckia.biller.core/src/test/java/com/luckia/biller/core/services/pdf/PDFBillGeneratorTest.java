package com.luckia.biller.core.services.pdf;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import javax.persistence.EntityManager;

import org.junit.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.luckia.biller.core.LuckiaCoreModule;
import com.luckia.biller.core.jpa.EntityManagerProvider;
import com.luckia.biller.core.model.Bill;

public class PDFBillGeneratorTest {

	@Test
	public void test() throws FileNotFoundException {
		Injector injector = Guice.createInjector(new LuckiaCoreModule());
		EntityManager entityManager = injector.getInstance(EntityManagerProvider.class).get();
		Bill bill = entityManager.createQuery("select b from Bill b order by b.code desc", Bill.class).setMaxResults(1).getSingleResult();
		FileOutputStream out = new FileOutputStream("./target/test.pdf");
		PDFBillGenerator generator = injector.getInstance(PDFBillGenerator.class);
		generator.generate(bill, out);
	}
}
