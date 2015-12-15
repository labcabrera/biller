package com.luckia.biller.core.jpa;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.persistence.EntityManager;

import org.eclipse.persistence.descriptors.ClassDescriptor;
import org.eclipse.persistence.queries.DataModifyQuery;
import org.eclipse.persistence.queries.ValueReadQuery;
import org.eclipse.persistence.sessions.Session;
import org.eclipse.persistence.sessions.server.ServerSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.luckia.biller.core.model.Sequence;

/**
 * Clase encargada de generar numeros secuenciales. Se utiliza por ejemplo para generar las secuencias con los codigos de las facturas.
 */
@Singleton
public class Sequencer {

	private static final Logger LOG = LoggerFactory.getLogger(Sequencer.class);

	private final Provider<EntityManager> entityManagerProvider;

	/** Para evitar que se generen dos secuencias iguales forzamos a que sea thread-safe a traves de este bloqueo */

	private final ReentrantLock lock;

	private final String tableName;

	private final String fieldName;

	private final String fieldValue;

	@Inject
	public Sequencer(Provider<EntityManager> entityManagerProvider) throws NoSuchFieldException, SecurityException {
		LOG.debug("Initializing sequencer");
		this.entityManagerProvider = entityManagerProvider;
		Session session = entityManagerProvider.get().unwrap(Session.class);
		ClassDescriptor desc = session.getClassDescriptor(Sequence.class);
		tableName = desc.getDefaultTable().getQualifiedName();
		fieldName = "ID";
		fieldValue = "VALUE";
		lock = new ReentrantLock();
	}

	public long nextSequence(String name) {
		lock.lock();
		long result;
		EntityManager entityManager = entityManagerProvider.get();
		ServerSession writeSession = entityManager.unwrap(ServerSession.class);
		boolean currentTransaction = entityManager.getTransaction().isActive();
		if (!currentTransaction) {
			writeSession.beginExternalTransaction();
			entityManager.getTransaction().begin();
		}
		List<?> queryParams = Arrays.asList(name);
		ValueReadQuery queryRead = new ValueReadQuery();
		String sqlRead = String.format("select %s from %s where %s = #%s", fieldValue, tableName, fieldName, fieldName);
		queryRead.addArgument(fieldName);
		queryRead.setSQLString(sqlRead);
		Number current = (Number) writeSession.executeQuery(queryRead, queryParams);
		if (current == null) {
			result = 1L;
			String sqlInsert = String.format("insert into %s (%s, %s) values(#%s, %s)", tableName, fieldName, fieldValue, fieldName, result);
			DataModifyQuery insertQuery = new DataModifyQuery();
			insertQuery.setSQLString(sqlInsert);
			insertQuery.addArgument(fieldName);
			writeSession.executeQuery(insertQuery, queryParams);
		} else {
			result = current.longValue() + 1L;
			DataModifyQuery queryUpdate = new DataModifyQuery();
			String sqlUpdate = String.format("update %s set %s = %s where %s = #%s", tableName, fieldValue, result, fieldName, fieldName);
			queryUpdate.addArgument(fieldName);
			queryUpdate.setSQLString(sqlUpdate);
			writeSession.executeQuery(queryUpdate, queryParams);
		}
		if (!currentTransaction) {
			writeSession.commitTransaction();
		}
		LOG.trace("Updated sequence {} for {} ({})", result, name, Thread.currentThread().getName());
		lock.unlock();
		return result;
	}
}
