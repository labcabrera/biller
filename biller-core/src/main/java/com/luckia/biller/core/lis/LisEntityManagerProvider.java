package com.luckia.biller.core.lis;

import java.io.InputStream;
import java.util.Properties;

import javax.inject.Provider;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import com.luckia.biller.core.Constants;

@Singleton
public class LisEntityManagerProvider implements Provider<EntityManager> {

	private static final String PERSISTENCE_UNIT_NAME = "com.cnp.saving.host";
	private static final String PERSISTENCE_PROPERTY_PREFIX = "dis.";

	private final ThreadLocal<EntityManager> entityManager;
	private final EntityManagerFactory entityManagerFactory;
	private final Properties persistenceProperties;

	public LisEntityManagerProvider() {
		entityManager = new ThreadLocal<EntityManager>();
		persistenceProperties = readHostProperties();
		entityManagerFactory = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME, persistenceProperties);
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

	private Properties readHostProperties() {
		try {
			Properties result = new Properties();
			Properties appProperties = new Properties();
			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			InputStream in = classLoader.getResourceAsStream(Constants.PROPERTIES_FILE);
			appProperties.load(in);
			for (Object object : appProperties.keySet()) {
				String key = (String) object;
				if (key.startsWith(PERSISTENCE_PROPERTY_PREFIX)) {
					result.put(key.substring(PERSISTENCE_PROPERTY_PREFIX.length()), appProperties.get(key));
				}
			}
			return result;
		} catch (Exception ex) {
			throw new RuntimeException("Error reading DB2 javax.persistence properties", ex);
		}
	}
}
