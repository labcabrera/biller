package com.luckia.biller.deploy.fedders;

import java.io.InputStream;
import java.math.BigDecimal;

import javax.inject.Inject;
import javax.persistence.EntityManager;

import com.luckia.biller.core.jpa.EntityManagerProvider;
import com.luckia.biller.core.model.BillingModel;
import com.luckia.biller.core.model.BillingModelType;

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
		entityManager.persist(modelA);

		BillingModel modelB = new BillingModel();
		modelB.setName("Módelo Estándar para Salones");
		modelB.setType(BillingModelType.Bill);
		modelB.setNrPercent(new BigDecimal("50.00"));
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
