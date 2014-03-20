package com.luckia.biller.core.jpa;

import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.FlushModeType;
import javax.persistence.Persistence;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class EntityManagerProvider {

	private static final Logger LOG = LoggerFactory.getLogger(EntityManagerProvider.class);

	private final ThreadLocal<EntityManager> entityManager;
	private final EntityManagerFactory entityManagerFactory;

	public EntityManagerProvider(String persistenceUnitName) {
		entityManager = new ThreadLocal<EntityManager>();
		entityManagerFactory = Persistence.createEntityManagerFactory(persistenceUnitName);
	}

	public EntityManager get() {
		synchronized (entityManagerFactory) {
			if (entityManager != null && entityManager.get() != null && entityManager.get().isOpen()) {
				return entityManager.get();
			} else {
				LOG.debug("Generando EntityManager para el thread {}", Thread.currentThread().getName());
				EntityManager instance = entityManagerFactory.createEntityManager();
				instance.setFlushMode(FlushModeType.COMMIT);
				entityManager.set(instance);
				return instance;
			}
		}
	}
}
