package com.luckia.biller.core.serialization;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.junit.Ignore;
import org.junit.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.persist.PersistService;
import com.luckia.biller.core.BillerModule;
import com.luckia.biller.core.model.Bill;

@Ignore
public class TestBillerSerializer {

	@Test
	public void test() {
		Injector injector = Guice.createInjector(new BillerModule());
		injector.getInstance(PersistService.class).start();
		EntityManager entityManager = injector.getProvider(EntityManager.class).get();
		Serializer serializer = injector.getInstance(Serializer.class);

		TypedQuery<Bill> query = entityManager.createQuery(
				"select b from Bill b join fetch b.details order by b.code desc",
				Bill.class);
		Bill bill = query.setMaxResults(1).getSingleResult();

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		serializer.serialize(bill, out);
		byte[] raw = out.toByteArray();
		System.out.println("Serialized:\n" + new String(raw));

		Bill value = serializer.deserialize(Bill.class, new ByteArrayInputStream(raw));
		System.out.println(value);
	}
}
