/*******************************************************************************
 * Copyright (c) 2004, 2012 Kotasoft S.L.
 * All rights reserved. This program and the accompanying materials
 * may only be used prior written consent of Kotasoft S.L.
 ******************************************************************************/
package com.luckia.biller.web.rest;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.luckia.biller.core.model.CostCenter;
import com.luckia.biller.core.model.common.Message;
import com.luckia.biller.core.model.common.SearchParams;
import com.luckia.biller.core.model.common.SearchResults;
import com.luckia.biller.core.services.entities.CostCenterEntityService;

@Path("costcenters")
public class CostCenterRestService {

	@Inject
	private CostCenterEntityService costCenterEntityService;

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("id/{id}")
	public CostCenter findById(@PathParam("id") Long primaryKey) {
		return costCenterEntityService.findById(primaryKey);
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/find")
	public SearchResults<CostCenter> find(@QueryParam("p") Integer page, @QueryParam("n") Integer itemsPerPage, @QueryParam("q") String queryString) {
		SearchParams searchParams = new SearchParams();
		searchParams.setItemsPerPage(itemsPerPage);
		searchParams.setCurrentPage(page);
		searchParams.setQueryString(queryString);
		return costCenterEntityService.find(searchParams);
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/merge")
	public Message<CostCenter> merge(CostCenter entity) {
		return costCenterEntityService.merge(entity);
	}

}
