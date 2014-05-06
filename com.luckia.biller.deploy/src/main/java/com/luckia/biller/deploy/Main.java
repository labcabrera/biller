package com.luckia.biller.deploy;

import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.apache.commons.lang3.Range;
import org.joda.time.DateTime;
import org.quartz.JobExecutionException;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.luckia.biller.core.LuckiaCoreModule;
import com.luckia.biller.core.jpa.EntityManagerProvider;
import com.luckia.biller.core.model.Address;
import com.luckia.biller.core.model.Company;
import com.luckia.biller.core.model.IdCard;
import com.luckia.biller.core.scheduler.BillingJob;
import com.luckia.biller.deploy.poi.MasterWorkbookProcessor;

/**
 * Clase encargada de generar el esquema de base de datos y realizar la carga inicial.
 */
public class Main {

	public static void main(String[] args) {
		if (args.length != 2) {
			System.out.println("Usage Main {generateBootstrap} {generateBills}");
		} else {
			boolean generateBootstrap = Boolean.parseBoolean(args[0]);
			boolean generateBills = Boolean.parseBoolean(args[1]);
			new Main().run(generateBootstrap, generateBills);
		}
	}

	private Injector injector;

	public void run(boolean generateBootstrap, boolean generateBills) {
		try {
			System.out.println("Biller deployment module");
			injector = Guice.createInjector(new LuckiaCoreModule());
			if (generateBootstrap) {
				System.out.println("Executing bootstrap");
				Bootstrap.main();
				MasterWorkbookProcessor.main();
				injector.getInstance(BillSequencePrefixGenerator.class).run();
				updateEgasaInfo();
			}
			if (generateBills) {
				System.out.println("Loading bills");
				generateBills();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * DESTINATARIO: Egasa Hattrick S.A. Arzobispo Fabian y Fuero 17B 46009 Valencia España Tel: 961100793 NIF: A98359169
	 */
	private void updateEgasaInfo() {
		EntityManager entityManager = injector.getInstance(EntityManagerProvider.class).get();
		TypedQuery<Company> query = entityManager.createQuery("select e from Company e where e.name like :name", Company.class);
		List<Company> list = query.setParameter("name", "%Egasa%").getResultList();
		if (!list.isEmpty()) {
			Company egasa = list.iterator().next();
			if (egasa.getAddress() == null) {
				egasa.setAddress(new Address());
			}
			egasa.getAddress().setRoad("Arzobispo Fabian y Fuero");
			egasa.getAddress().setNumber("17B");
			egasa.getAddress().setZipCode("46009");
			egasa.setPhoneNumber("961100793");
			if (egasa.getIdCard() == null) {
				egasa.setIdCard(new IdCard());
			}
			egasa.getIdCard().setNumber("A98359169");
			egasa.setAccountNumber("ES29 0182 6205 16 0201501686");
			entityManager.getTransaction().begin();
			entityManager.merge(egasa);
			entityManager.getTransaction().commit();
		}
	}

	private void generateBills() throws JobExecutionException {
		System.out.println("Generando facturacion de enero");
		BillingJob job = injector.getInstance(BillingJob.class);
		job.setInjector(injector);
		Date from;
		Date to;

		from = new DateTime(2014, 1, 1, 0, 0, 0, 0).toDate();
		to = new DateTime(2014, 1, 31, 0, 0, 0, 0).toDate();
		job.execute(Range.between(from, to), 10);

		from = new DateTime(2014, 2, 1, 0, 0, 0, 0).toDate();
		to = new DateTime(2014, 2, 28, 0, 0, 0, 0).toDate();
		job.execute(Range.between(from, to), 10);

		from = new DateTime(2014, 3, 1, 0, 0, 0, 0).toDate();
		to = new DateTime(2014, 3, 31, 0, 0, 0, 0).toDate();
		job.execute(Range.between(from, to), 10);
		
		from = new DateTime(2014, 4, 1, 0, 0, 0, 0).toDate();
		to = new DateTime(2014, 4, 30, 0, 0, 0, 0).toDate();
		job.execute(Range.between(from, to), 10);
	}
}
