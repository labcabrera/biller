package com.luckia.biller.core;

import java.util.Properties;

import org.apache.bval.guice.ValidationModule;

import com.google.inject.AbstractModule;
import com.google.inject.matcher.Matchers;
import com.google.inject.name.Names;
import com.luckia.biller.core.jpa.EntityManagerProvider;
import com.luckia.biller.core.services.bills.BillDataProvider;
import com.luckia.biller.core.services.bills.BillProcessor;
import com.luckia.biller.core.services.bills.LiquidationProcessor;
import com.luckia.biller.core.services.bills.impl.BillProcessorImpl;
import com.luckia.biller.core.services.bills.impl.LiquidationProcessorImpl;
import com.luckia.biller.core.services.bills.impl.LISBillDataProvider;
import com.luckia.biller.core.validation.LegalEntityValidator;

/**
 * Modulo de Guice principal de la aplicacion. Este modulo:
 * <ul>
 * <li>Inicia los componentes de JPA (tanto para el persistence unit de la aplicacion como para el de la comunicacion con LIS)</li>
 * <li>Registra las implementaciones asociadas a las interfaces definidas en la aplicacion</li>
 * <li>Carga el fichero de propiedades del sistema</li>
 * <li>Registra el módulo de validacion de Apache BVal (JSR 303)</li>
 * </ul>
 */
public class MainModule extends AbstractModule {

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.google.inject.AbstractModule#configure()
	 */
	@Override
	public void configure() {
		configureInterceptors();
		bindProperties();
		bindEntityManagers();
		bind(BillProcessor.class).to(BillProcessorImpl.class);
		bind(LiquidationProcessor.class).to(LiquidationProcessorImpl.class);
		bind(BillDataProvider.class).to(LISBillDataProvider.class);
		install(new ValidationModule());
		bind(LegalEntityValidator.class);
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

	/**
	 * Carga el {@link Properties} de la aplicacion para poder acceder a ellas a traves de las anotaciones @Named para los tipos
	 * <code>String</code>. Por ejemplo:
	 * 
	 * <pre>
	 * &#064;Inject
	 * &#064;Named(&quot;job.biller.cron&quot;)
	 * private String cronBillerJob;
	 * </pre>
	 * 
	 * habiendo configurado en el fichero de propiedades por ejemplo:
	 * 
	 * <pre>
	 * job.biller.cron=0 0/1 * 1/1 * ? *
	 * </pre>
	 */
	protected void bindProperties() {
		try {
			Properties properties = new Properties();
			properties.load(getClassLoader().getResourceAsStream(Constants.PROPERTIES_FILE));
			Names.bindProperties(binder(), properties);
		} catch (Exception ex) {
			throw new RuntimeException("Error loading app properties", ex);
		}
	}

	protected void configureInterceptors() {
		ClearCacheInterceptor clearCacheInterceptor = new ClearCacheInterceptor();
		requestInjection(clearCacheInterceptor);
		bindInterceptor(Matchers.any(), Matchers.annotatedWith(ClearCache.class), clearCacheInterceptor);
	}

	protected ClassLoader getClassLoader() {
		return getClass().getClassLoader();
	}
}
