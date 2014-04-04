package com.luckia.biller.core.jpa;

import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.FlushModeType;
import javax.persistence.Persistence;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Singleton encargado de gestionar la recuperación de los {@link EntityManager} de JPA. Para ello instancia un único EntityManagerFactory y
 * asocia a cada thread que lo solicite su propio EntityManager. <br>
 * De este modo si tenemos por ejemplo 4 threads operando de forma concurrente cada uno de ellos tendrá su propia instancia de EntityManager
 * que funcionara independientemente de las otras instancias.
 */
@Singleton
public class EntityManagerProvider {

	private static final Logger LOG = LoggerFactory.getLogger(EntityManagerProvider.class);

	private final ThreadLocal<EntityManager> entityManager;
	private final EntityManagerFactory entityManagerFactory;

	/**
	 * Constructor a partir del nombre de persistencia. Estos se establecen en el fichero <code>META-INF/persistence.xml</code>
	 * @param persistenceUnitName
	 */
	public EntityManagerProvider(String persistenceUnitName) {
		entityManager = new ThreadLocal<EntityManager>();
		entityManagerFactory = Persistence.createEntityManagerFactory(persistenceUnitName);
	}

	/**
	 * Obtiene el EntityManager
	 * 
	 * @return
	 */
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
