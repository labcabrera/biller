/*******************************************************************************
 * Copyright (c) 2004, 2012 Kotasoft S.L.
 * All rights reserved. This program and the accompanying materials
 * may only be used prior written consent of Kotasoft S.L.
 ******************************************************************************/
package com.luckia.biller.core.services.entities;

import org.junit.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.luckia.biller.core.BillerModule;
import com.luckia.biller.core.model.Bill;
import com.luckia.biller.core.model.common.SearchParams;
import com.luckia.biller.core.model.common.SearchResults;

public class BillServiceTest {

	@Test
	public void test() {
		Injector injector = Guice.createInjector(new BillerModule());
		BillEntityService billService = injector.getInstance(BillEntityService.class);
		SearchParams searchParams = new SearchParams();
		searchParams.setCurrentPage(1);
		searchParams.setItemsPerPage(2);
		searchParams.setQueryString("receiver.name=lk=Egasa");
		SearchResults<Bill> results = billService.find(searchParams);
		System.out.println("Current page   : " + results.getCurrentPage());
		System.out.println("Items per page : " + results.getItemsPerPage());
		System.out.println("Total items    : " + results.getTotalItems());
		System.out.println("Total pages    : " + results.getTotalPages());
		System.out.println("Results:");
		for (Bill i : results.getResults()) {
			System.out.println(i.getCode() + ":");
		}
	}
}
