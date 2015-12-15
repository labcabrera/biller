package com.luckia.biller.core.services;

import org.junit.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.luckia.biller.core.BillerModule;

public class TestAlertService {

	@Test
	public void test() throws InterruptedException {
		Injector injector = Guice.createInjector(new BillerModule());
		AlertService alertService = injector.getInstance(AlertService.class);
		alertService.handleAlert("Dummy message");
		Thread.sleep(100000000000000000L);
	}
}
