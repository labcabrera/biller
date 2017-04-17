package com.luckia.biller.deploy.fedders;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.persistence.EntityManager;

import com.luckia.biller.core.model.ProvinceTaxes;
import com.luckia.biller.core.model.Province;

public class BillingProvinceFeesFeeder implements Feeder<ProvinceTaxes> {

	@Inject
	private Provider<EntityManager> entityManagerProvider;

	@Override
	public void loadEntities(InputStream source) {
		EntityManager entityManager = entityManagerProvider.get();
		List<Province> provinces = entityManager
				.createQuery("select p from Province p", Province.class).getResultList();
		for (Province province : provinces) {
			ProvinceTaxes fees = new ProvinceTaxes();
			fees.setFeesPercent(new BigDecimal("10.00"));
			fees.setProvince(province);
			entityManager.persist(fees);
		}
	}
}
