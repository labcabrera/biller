package com.luckia.biller.web.servlet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.luckia.biller.core.BillerModule;
import com.luckia.biller.web.json.GsonMessageBodyHandler;
import com.luckia.biller.web.rest.AlertReceiverRestService;
import com.luckia.biller.web.rest.BillLiquidationDetailRestService;
import com.luckia.biller.web.rest.BillRestService;
import com.luckia.biller.web.rest.BillingModelRestService;
import com.luckia.biller.web.rest.BinaryRestService;
import com.luckia.biller.web.rest.CompanyGroupRestService;
import com.luckia.biller.web.rest.CompanyRestService;
import com.luckia.biller.web.rest.CostCenterRestService;
import com.luckia.biller.web.rest.DashboardRestService;
import com.luckia.biller.web.rest.EnumerationRestService;
import com.luckia.biller.web.rest.LiquidationDetailRestService;
import com.luckia.biller.web.rest.LiquidationRestService;
import com.luckia.biller.web.rest.OwnerRestService;
import com.luckia.biller.web.rest.ProvinceTaxesRestService;
import com.luckia.biller.web.rest.ProvincesRestService;
import com.luckia.biller.web.rest.RappelStoreRestService;
import com.luckia.biller.web.rest.RecalculationRestService;
import com.luckia.biller.web.rest.RegionsRestService;
import com.luckia.biller.web.rest.ReportRestService;
import com.luckia.biller.web.rest.SchedulerRestService;
import com.luckia.biller.web.rest.SecurityRestService;
import com.luckia.biller.web.rest.StoreRestService;
import com.luckia.biller.web.rest.TerminalRelationRestService;
import com.luckia.biller.web.rest.UserActivityRestService;
import com.luckia.biller.web.rest.UserRestService;
import com.luckia.biller.web.security.SecurityInterceptor;

/**
 * Modulo de Guice encargado de registrar los servicios REST que expone la aplicacion.
 */
public class RestModule implements Module {

	private static final Logger LOG = LoggerFactory.getLogger(RestModule.class);

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.google.inject.Module#configure(com.google.inject.Binder)
	 */
	@Override
	public void configure(final Binder binder) {
		LOG.debug("Configuring Guice Module");
		binder.install(new BillerModule());
		// Components
		binder.bind(RestPersistFilter.class);
		binder.bind(GsonMessageBodyHandler.class);
		// Security
		binder.bind(SecurityInterceptor.class);
		// Rest services
		binder.bind(AlertReceiverRestService.class);
		binder.bind(BillingModelRestService.class);
		binder.bind(BillLiquidationDetailRestService.class);
		binder.bind(BillRestService.class);
		binder.bind(BinaryRestService.class);
		binder.bind(CompanyGroupRestService.class);
		binder.bind(CompanyRestService.class);
		binder.bind(CostCenterRestService.class);
		binder.bind(DashboardRestService.class);
		binder.bind(EnumerationRestService.class);
		binder.bind(LiquidationDetailRestService.class);
		binder.bind(LiquidationRestService.class);
		binder.bind(OwnerRestService.class);
		binder.bind(ProvincesRestService.class);
		binder.bind(ProvinceTaxesRestService.class);
		binder.bind(RappelStoreRestService.class);
		binder.bind(RecalculationRestService.class);
		binder.bind(RegionsRestService.class);
		binder.bind(ReportRestService.class);
		binder.bind(SchedulerRestService.class);
		binder.bind(SecurityRestService.class);
		binder.bind(StoreRestService.class);
		binder.bind(TerminalRelationRestService.class);
		binder.bind(UserActivityRestService.class);
		binder.bind(UserRestService.class);
	}
}
