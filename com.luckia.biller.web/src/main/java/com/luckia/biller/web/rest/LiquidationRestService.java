/*******************************************************************************
 * Copyright (c) 2004, 2012 Kotasoft S.L.
 * All rights reserved. This program and the accompanying materials
 * may only be used prior written consent of Kotasoft S.L.
 ******************************************************************************/
package com.luckia.biller.web.rest;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.apache.commons.lang.NotImplementedException;

import com.google.inject.Inject;
import com.luckia.biller.core.ClearCache;
import com.luckia.biller.core.i18n.I18nService;
import com.luckia.biller.core.jpa.EntityManagerProvider;
import com.luckia.biller.core.model.Bill;
import com.luckia.biller.core.model.Liquidation;
import com.luckia.biller.core.model.common.Message;
import com.luckia.biller.core.model.common.SearchParams;
import com.luckia.biller.core.model.common.SearchResults;
import com.luckia.biller.core.services.bills.LiquidationProcessor;
import com.luckia.biller.core.services.entities.LiquidationEntityService;

@Path("liquidations")
public class LiquidationRestService {

	@Inject
	private LiquidationEntityService entityService;
	@Inject
	private LiquidationProcessor liquidationProcessor;
	@Inject
	private EntityManagerProvider entityManagerProvider;
	@Inject
	private I18nService i18nService;

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/id/{id}")
	@ClearCache
	public Liquidation findById(@PathParam("id") String primaryKey) {
		return entityService.findById(primaryKey);
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/find")
	@ClearCache
	public SearchResults<Liquidation> find(@QueryParam("n") Integer maxResults, @QueryParam("p") Integer page, @QueryParam("q") String queryString) {
		SearchParams params = new SearchParams();
		params.setItemsPerPage(maxResults);
		params.setCurrentPage(page);
		params.setQueryString(queryString);
		return entityService.find(params);
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/merge")
	@ClearCache
	public Message<Liquidation> merge(Bill bill) {
		throw new NotImplementedException();
	}

	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Path("confirm/{id}")
	@ClearCache
	public Message<Liquidation> confirm(@PathParam("id") String id) {
		try {
			Liquidation liquidation = entityManagerProvider.get().find(Liquidation.class, id);
			liquidationProcessor.confirm(liquidation);
			return new Message<Liquidation>(Message.CODE_SUCCESS, i18nService.getMessage("liquidation.confirm.success"), liquidation);
		} catch (Exception ex) {
			return new Message<Liquidation>(Message.CODE_SUCCESS, i18nService.getMessage("liquidation.confirm.error"));
		}
	}
}
