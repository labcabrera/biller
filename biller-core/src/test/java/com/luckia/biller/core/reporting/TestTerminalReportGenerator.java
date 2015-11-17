package com.luckia.biller.core.reporting;

import org.junit.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.persist.PersistService;
import com.luckia.biller.core.BillerModule;
import com.luckia.biller.core.model.Company;

public class TestTerminalReportGenerator {

	@Test
	public void test() {
		Injector injector = Guice.createInjector(new BillerModule());
		injector.getInstance(PersistService.class).start();
		TerminalReportGenerator service = injector.getInstance(TerminalReportGenerator.class);
		Company company = new Company();
		company.setId(11084L);
		service.generate(null, company, null);
	}

}
