package com.luckia.biller.core;

import java.util.Properties;

import org.apache.bval.guice.ValidationModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.AbstractModule;
import com.google.inject.matcher.Matchers;
import com.google.inject.name.Names;
import com.google.inject.persist.jpa.JpaPersistModule;
import com.luckia.biller.core.common.RegisterActivity;
import com.luckia.biller.core.common.SettingsManager;
import com.luckia.biller.core.lis.LisModule;
import com.luckia.biller.core.services.UserActivityInterceptor;
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
 * <li>Inicia los componentes de JPA (tanto para el persistence unit de la aplicacion como
 * para el de la comunicacion con LIS)</li>
 * <li>Registra las implementaciones asociadas a las interfaces definidas en la
 * aplicacion</li>
 * <li>Registra el módulo de validacion de Apache BVal (JSR 303)</li>
 * </ul>
 */
public class BillerModule extends AbstractModule {

	private static final Logger LOG = LoggerFactory.getLogger(BillerModule.class);

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.google.inject.AbstractModule#configure()
	 */
	@Override
	public void configure() {
		LOG.debug("Configuring Biller core module");
		SettingsManager settingsManager = new SettingsManager().load();
		bind(SettingsManager.class).toInstance(settingsManager);
		Names.bindProperties(binder(),
				settingsManager.getProperties(Constants.CONFIG_SECTION_GLOBAL));
		installJpaModule(settingsManager);
		install(new ValidationModule());
		install(new LisModule());
		bind(BillProcessor.class).to(BillProcessorImpl.class);
		bind(LiquidationProcessor.class).to(LiquidationProcessorImpl.class);
		bind(RappelStoreProcessor.class).to(RappelStoreProcessorImpl.class);
		bind(BillDataProvider.class).to(LISBillDataProvider.class);
		bind(LegalEntityValidator.class);
		UserActivityInterceptor userActivityInterceptor = new UserActivityInterceptor();
		requestInjection(userActivityInterceptor);
		bindInterceptor(Matchers.any(), Matchers.annotatedWith(RegisterActivity.class),
				userActivityInterceptor);
		LOG.debug("Configured Biller core module");
	}

	protected void installJpaModule(SettingsManager settingsManager) {
		Properties properties = settingsManager
				.getProperties(Constants.CONFIG_SECTION_JPA_BILLER);
		JpaPersistModule module = new JpaPersistModule(Constants.PERSISTENCE_UNIT_NAME);
		module.properties(properties);
		install(module);
	}
}
