package com.luckia.biller.core.integration;

import org.apache.camel.builder.RouteBuilder;

public class BillerRouteBuilder extends RouteBuilder {

	public static final String IN_BILL_CONFIRM = "direct:in_bill_confirmation";
	public static final String IN_BILL_RECALCULATION = "direct:in_bill_recalculation";
	public static final String IN_LIQUIDATION_CONFIRM = "direct:in_liquidation_confirmation";
	public static final String IN_LIQUIDATION_RECALCULATION = "direct:in_liquidation_recalculation";

	@Override
	public void configure() throws Exception {

		from(IN_BILL_CONFIRM) //
				// .bean(PaymentValidator.class) //
				// .bean(FeesProcessor.class) //
				// .bean(GenerateMarketOrders.class) //
				// .bean(ProcessOrder.class) //
				// // Enviamos esta tarea a otra cola para no esperar para generar
				// // la respuesta
				// .multicast().parallelProcessing().to(Routes.ORDER_PROCESSING_QUEUE) //
				.bean(JsonMessageSerializer.class);
		//
		// from(Routes.ORDER_PROCESSING_QUEUE) //
		// .delay(3000L) //
		// .choice() //
		// // Servicios especificos de pagos
		// .when(new ClassPredicate(Payment.class)) //
		// .bean(ProcessPayment.class) //
		// .endChoice() //
		// .bean(GenerateSendOrders.class) //
		// .to(Routes.ORDER_VALORIZATION_QUEUE);
		//
		// from(Routes.ORDER_VALORIZATION_QUEUE) //
		// .bean(ValorizationOrder.class);
		//
		// from("file://target/input/001").beanRef("mathProvisionLoader");
		//
		// from("file://target/input/002") //
		// .routeId("SAMPLE_ROUTE_002") //
		// // Controlamos exceptiones recuperables
		// .onException(RecoverableServiceException.class) //
		// .maximumRedeliveries(1) //
		// .redeliveryDelay(1000L) //
		// .logRetryAttempted(true) //
		// .logRetryStackTrace(true) //
		// .retryAttemptedLogLevel(LoggingLevel.WARN).end() //
		// // Controlamos excepciones no recuperables
		// .onException(NonRecoverableServiceException.class) //
		// .log(LoggingLevel.ERROR, "Terminal error processing ${in.body}. Failing-fast by forwarding to error destination.
		// ${exception.stacktrace}") //
		// .to(Routes.ERROR_QUEUE) //
		// .handled(true) //
		// .end() //
		// .bean(JsonMessageSerializer.class);
		//
	}

}
