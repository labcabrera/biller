package com.luckia.biller.web;

import org.junit.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.persist.PersistService;
import com.luckia.biller.web.servlet.RestModule;

public class TestRestModule {

	@Test
	public void test() {
		RestModule module = new RestModule();
		Injector injector = Guice.createInjector(module);
		injector.getInstance(PersistService.class).start();
	}
}
