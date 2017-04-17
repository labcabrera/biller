package com.luckia.biller.deploy;

import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Inject;
import javax.persistence.EntityManager;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.persist.PersistService;
import com.luckia.biller.core.BillerModule;
import com.luckia.biller.deploy.fedders.BillingModelFeeder;
import com.luckia.biller.deploy.fedders.BillingProvinceFeesFeeder;
import com.luckia.biller.deploy.fedders.CompanyGroupFeeder;
import com.luckia.biller.deploy.fedders.CostCenterFeeder;
import com.luckia.biller.deploy.fedders.Feeder;
import com.luckia.biller.deploy.fedders.ProvinceFeeder;
import com.luckia.biller.deploy.fedders.RegionFeeder;
import com.luckia.biller.deploy.fedders.StateDefinitionFeeder;
import com.luckia.biller.deploy.fedders.UserFeeder;

public class Bootstrap implements Runnable {

	private static final Logger LOG = LoggerFactory.getLogger(Bootstrap.class);

	@Inject
	private Injector injector;

	public static void main(String... args) {
		Injector injector = Guice.createInjector(new BillerModule());
		injector.getInstance(PersistService.class).start();
		Bootstrap bootstrap = injector.getInstance(Bootstrap.class);
		bootstrap.run();
	}

	@Override
	public void run() {
		LOG.info("Starting Bootstrap");
		Validate.notNull(injector);
		EntityManager entityManager = injector.getProvider(EntityManager.class).get();
		entityManager.getTransaction().begin();
		if (isDatabaseInitialized()) {
			LOG.info("Database already initialized");
		}
		else {
			Map<Class<? extends Feeder<?>>, String> feederMapping = new LinkedHashMap<Class<? extends Feeder<?>>, String>();
			feederMapping.put(UserFeeder.class, "bootstrap/users.csv");
			feederMapping.put(ProvinceFeeder.class, "bootstrap/provinces.json");
			feederMapping.put(RegionFeeder.class, "bootstrap/regions.csv");
			feederMapping.put(CompanyGroupFeeder.class, "bootstrap/company-groups.csv");
			feederMapping.put(CostCenterFeeder.class, "bootstrap/cost-centers.csv");
			feederMapping.put(BillingModelFeeder.class, "bootstrap/billing-models.csv");
			feederMapping.put(StateDefinitionFeeder.class, "");
			feederMapping.put(BillingProvinceFeesFeeder.class, "");
			Long t0 = System.currentTimeMillis();
			ClassLoader classLoader = getClass().getClassLoader();
			for (Entry<Class<? extends Feeder<?>>, String> entry : feederMapping
					.entrySet()) {
				InputStream inputStream = null;
				Class<? extends Feeder<?>> clazz = entry.getKey();
				String resource = entry.getValue();
				if (StringUtils.isNotBlank(resource)) {
					inputStream = classLoader
							.getResourceAsStream(feederMapping.get(clazz));
				}
				injector.getInstance(clazz).loadEntities(inputStream);
			}
			entityManager.getTransaction().commit();
			LOG.info("Bootstrap completed in {} ms", System.currentTimeMillis() - t0);
		}
	}

	private Boolean isDatabaseInitialized() {
		return getEntityManager().createQuery("select count(u) from User u", Long.class)
				.getSingleResult() > 0;
	}

	private EntityManager getEntityManager() {
		return injector.getProvider(EntityManager.class).get();
	}
}
