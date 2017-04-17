package com.luckia.biller.core.common;

import java.sql.Connection;
import java.sql.SQLException;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.persistence.EntityManager;

import org.eclipse.persistence.sessions.server.ServerSession;

import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.resource.ResourceAccessor;
import lombok.extern.slf4j.Slf4j;

/**
 * Componente que se encarga de comprobar el fichero cambios de base de datos de Liquibase
 * para comprobar si es necesario ejecutar algun changelog.
 */
@Slf4j
public class LiquibaseSchemaChecker {

	private static final String DEFAULT_MASTER_CHANGELOG = "dbchangelog/db-master.xml";

	@Inject
	private Provider<EntityManager> entityManagerProvider;

	public void checkSchema() throws LiquibaseException {
		checkSchema(DEFAULT_MASTER_CHANGELOG);
	}

	public void checkSchema(String schema) throws LiquibaseException {
		Connection connection = resolveConnection();
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		ResourceAccessor resourceAccessor = new ClassLoaderResourceAccessor(classLoader);
		Database database = DatabaseFactory.getInstance()
				.findCorrectDatabaseImplementation(new JdbcConnection(connection));
		Liquibase liquibase = new Liquibase(schema, resourceAccessor, database);
		liquibase.forceReleaseLocks();
		log.info("Comprobando actualizaciones de esquema de BBDD");
		liquibase.update("");
		log.info("Comprobacion de esquema de BBDD finalizada");
		try {
			connection.close();
		}
		catch (SQLException ignore) {
			log.trace("Connection close error", ignore);
		}
	}

	private Connection resolveConnection() {
		return entityManagerProvider.get().unwrap(ServerSession.class).getAccessor()
				.getConnection();
	}
}
