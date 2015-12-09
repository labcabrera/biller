package com.luckia.biller.core.lis;

import java.util.Properties;

import javax.inject.Provider;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import com.luckia.biller.core.Constants;
import com.luckia.biller.core.common.SettingsManager;

@Singleton
public class LisEntityManagerProvider implements Provider<EntityManager> {

	private final ThreadLocal<EntityManager> entityManager;
	private final EntityManagerFactory entityManagerFactory;
	private final Properties persistenceProperties;

	public LisEntityManagerProvider() {
		try {
			entityManager = new ThreadLocal<EntityManager>();
			persistenceProperties = readLisProperties();
			entityManagerFactory = Persistence.createEntityManagerFactory(Constants.PERSISTENCE_UNIT_NAME_LIS, persistenceProperties);
		} catch (Exception ex) {
			throw new RuntimeException("Error loading Lis entity manager provider", ex);
		}
	}

	@Override
	public EntityManager get() {
		EntityManager em = entityManager.get();
		if (em == null) {
			em = entityManagerFactory.createEntityManager();
			entityManager.set(em);
		}
		return em;
	}

	private Properties readLisProperties() {
		return new SettingsManager().load().getProperties(Constants.CONFIG_SECTION_JPA_LIS);
	}
}
