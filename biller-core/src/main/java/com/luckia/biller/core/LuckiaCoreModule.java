package com.luckia.biller.core;

import org.apache.bval.guice.ValidationModule;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import com.google.inject.persist.jpa.JpaPersistModule;
import com.luckia.biller.core.jpa.EntityManagerProvider;
import com.luckia.biller.core.lis.LisModule;
import com.luckia.biller.core.services.bills.BillDataProvider;
import com.luckia.biller.core.services.bills.BillProcessor;
import com.luckia.biller.core.services.bills.LiquidationProcessor;
import com.luckia.biller.core.services.bills.RappelStoreProcessor;
import com.luckia.biller.core.services.bills.impl.BillProcessorImpl;
import com.luckia.biller.core.services.bills.impl.LISBillDataProvider;
import com.luckia.biller.core.services.bills.impl.LiquidationProcessorImpl;
import com.luckia.biller.core.services.bills.impl.RappelStoreProcessorImpl;
import com.luckia.biller.core.validation.LegalEntityValidator;

/**
 * Modulo de Guice principal de la aplicacion. Este modulo:
 * <ul>
 * <li>Inicia los componentes de JPA (tanto para el persistence unit de la aplicacion como para el de la comunicacion con LIS)</li>
 * <li>Registra las implementaciones asociadas a las interfaces definidas en la aplicacion</li>
 * <li>Registra el módulo de validacion de Apache BVal (JSR 303)</li>
 * </ul>
 */
public class LuckiaCoreModule extends AbstractModule {

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.google.inject.AbstractModule#configure()
	 */
	@Override
	public void configure() {
		install(new JpaPersistModule(Constants.PERSISTENCE_UNIT_NAME));
		bindEntityManagers();
		bind(BillProcessor.class).to(BillProcessorImpl.class);
		bind(LiquidationProcessor.class).to(LiquidationProcessorImpl.class);
		bind(RappelStoreProcessor.class).to(RappelStoreProcessorImpl.class);
		bind(BillDataProvider.class).to(LISBillDataProvider.class);
		bind(LegalEntityValidator.class);
		install(new ValidationModule());
		install(new LisModule());
	}

	/**
	 * Establece el {@link EntityManagerProvider} principal de la aplicacion y registra otro anotado como <code>@Named("LIS")</code> para
	 * acceder a la base de datos de LIS.
	 */
	protected void bindEntityManagers() {
		EntityManagerProvider mainEntityManagerProvider = new EntityManagerProvider(Constants.PERSISTENCE_UNIT_NAME);
		EntityManagerProvider lisEntityManagerProvider = new EntityManagerProvider(Constants.PERSISTENCE_UNIT_NAME_LIS);
		bind(EntityManagerProvider.class).toInstance(mainEntityManagerProvider);
		bind(EntityManagerProvider.class).annotatedWith(Names.named(Constants.LIS)).toInstance(lisEntityManagerProvider);
	}

	protected ClassLoader getClassLoader() {
		return getClass().getClassLoader();
	}
}
