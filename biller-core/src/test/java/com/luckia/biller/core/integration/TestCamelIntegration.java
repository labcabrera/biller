package com.luckia.biller.core.integration;

import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.junit.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.luckia.biller.core.BillerModule;
import com.luckia.biller.core.model.Bill;
import com.luckia.biller.core.model.common.Message;
import com.luckia.biller.core.serialization.Serializer;

public class TestCamelIntegration {

	@Test
	public void test() {
		Injector injector = Guice.createInjector(new BillerModule());

		CamelContext camelContext = injector.getInstance(CamelContext.class);
		Serializer serializer = injector.getInstance(Serializer.class);

		Bill bill = new Bill();
		bill.setId("123");

		ProducerTemplate producer = camelContext.createProducerTemplate();
		Object result = producer.requestBody(BillerRouteBuilder.IN_BILL_CONFIRM, bill, Message.class);

		System.out.println(serializer.toJson(result));
	}

}
