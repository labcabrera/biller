package com.luckia.biller.core.lis;

import java.io.InputStream;
import java.util.Properties;

import javax.inject.Provider;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.luckia.biller.core.Constants;

@Singleton
public class LisEntityManagerProvider implements Provider<EntityManager> {

	private static final Logger LOG = LoggerFactory.getLogger(LisEntityManagerProvider.class);

	private final ThreadLocal<EntityManager> entityManager;
	private final EntityManagerFactory entityManagerFactory;
	private final Properties persistenceProperties;

	public LisEntityManagerProvider() {
		entityManager = new ThreadLocal<EntityManager>();
		persistenceProperties = readHostProperties();
		entityManagerFactory = Persistence.createEntityManagerFactory(Constants.PERSISTENCE_UNIT_NAME_LIS, persistenceProperties);
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
				if (key.startsWith(Constants.PROPERTIES_LIS_PREFIX)) {
					String lisKey = key.substring(Constants.PROPERTIES_LIS_PREFIX.length());
					String lisValue = (String) appProperties.get(key);
					LOG.debug("Setting LIS data base connection property {}: {}", lisKey, lisValue);
					result.put(lisKey, lisValue);
				}
			}
			return result;
		} catch (Exception ex) {
			throw new RuntimeException("Error reading DB2 javax.persistence properties", ex);
		}
	}
}
