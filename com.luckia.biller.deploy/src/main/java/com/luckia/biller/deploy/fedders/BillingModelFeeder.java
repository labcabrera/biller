package com.luckia.biller.deploy.fedders;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;

import javax.inject.Inject;
import javax.persistence.EntityManager;

import com.luckia.biller.core.jpa.EntityManagerProvider;
import com.luckia.biller.core.model.BillingModel;
import com.luckia.biller.core.model.BillingModelType;
import com.luckia.biller.core.model.Rappel;

public class BillingModelFeeder implements Feeder<BillingModel> {

	@Inject
	private EntityManagerProvider entityManagerProvider;

	@Override
	public void loadEntities(InputStream source) {
		EntityManager entityManager = entityManagerProvider.get();

		BillingModel modelA = new BillingModel();
		modelA.setName("Módelo Estándar para Bares");
		modelA.setType(BillingModelType.Bill);
		modelA.setGgrPercent(new BigDecimal("1.00"));
		modelA.setRappel(new ArrayList<Rappel>());
		modelA.getRappel().add(new Rappel(new BigDecimal("055000"), new BigDecimal("0.50"), new BigDecimal("0275")));
		modelA.getRappel().add(new Rappel(new BigDecimal("075000"), new BigDecimal("1.00"), new BigDecimal("0750")));
		modelA.getRappel().add(new Rappel(new BigDecimal("100000"), new BigDecimal("1.50"), new BigDecimal("1500")));
		modelA.getRappel().add(new Rappel(new BigDecimal("125000"), new BigDecimal("1.75"), new BigDecimal("2188")));
		modelA.getRappel().add(new Rappel(new BigDecimal("150000"), new BigDecimal("2.00"), new BigDecimal("3000")));
		for (Rappel i : modelA.getRappel()) {
			i.setModel(modelA);
		}
		entityManager.persist(modelA);

		BillingModel modelB = new BillingModel();
		modelB.setName("Módelo Estándar para Salones");
		modelB.setType(BillingModelType.Bill);
		modelB.setNrPercent(new BigDecimal("50.00"));
		modelB.setRappel(new ArrayList<Rappel>());
		modelB.getRappel().add(new Rappel(new BigDecimal("0350000"), null, new BigDecimal("07000")));
		modelB.getRappel().add(new Rappel(new BigDecimal("0500000"), null, new BigDecimal("10000")));
		modelB.getRappel().add(new Rappel(new BigDecimal("0750000"), null, new BigDecimal("15000")));
		modelB.getRappel().add(new Rappel(new BigDecimal("1000000"), null, new BigDecimal("20000")));
		modelB.getRappel().add(new Rappel(new BigDecimal("1250000"), null, new BigDecimal("25000")));
		modelB.getRappel().add(new Rappel(new BigDecimal("1500000"), null, new BigDecimal("30000")));
		for (Rappel i : modelB.getRappel()) {
			i.setModel(modelB);
		}
		entityManager.persist(modelB);

		BillingModel modelC = new BillingModel();
		modelC.setName("Módelo Salones Videomani");
		modelC.setType(BillingModelType.Bill);
		modelC.setNgrPercent(new BigDecimal("50.00"));
		entityManager.persist(modelC);

		BillingModel modelD = new BillingModel();
		modelD.setName("Módelo Bares Videomani");
		modelC.setNgrPercent(new BigDecimal("50.00"));
		entityManager.persist(modelC);

		BillingModel modelE = new BillingModel();
		modelE.setName("Modelo Salones Binelde");
		modelE.setType(BillingModelType.Bill);
		modelC.setNrPercent(new BigDecimal("60.00"));
		entityManager.persist(modelE);

		entityManager.flush();
	}
}
