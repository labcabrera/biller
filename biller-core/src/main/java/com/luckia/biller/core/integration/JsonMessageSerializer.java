package com.luckia.biller.core.integration;

import javax.inject.Inject;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

import com.luckia.biller.core.model.common.Message;
import com.luckia.biller.core.serialization.Serializer;

public class JsonMessageSerializer implements Processor {

	@Inject
	private Serializer serializer;

	public void process(Exchange exchange) {
		Object body = exchange.getIn().getBody();
		String json = serializer.toJson(body);
		if (!exchange.isFailed() && exchange.getIn().getBody() != null) {
			exchange.getIn().setBody(new Message<Object>("Success", json));
		} else {
			exchange.getIn().setBody(new Message<Object>("Error", json));
		}
	}

}
