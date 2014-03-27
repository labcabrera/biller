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

import com.luckia.biller.core.i18n.I18nService;
import com.luckia.biller.core.model.CompanyGroup;
import com.luckia.biller.core.model.common.Message;
import com.luckia.biller.core.model.common.SearchParams;
import com.luckia.biller.core.model.common.SearchResults;
import com.luckia.biller.core.services.entities.CompanyGroupEntityService;

@Path("groups")
public class CompanyGroupRestService {

	@Inject
	private CompanyGroupEntityService companyGroupService;
	@Inject
	private I18nService i18nService;

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/id/{id}")
	public CompanyGroup findById(@PathParam("id") Long primaryKey) {
		return companyGroupService.findById(primaryKey);
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/find")
	public SearchResults<CompanyGroup> find(@QueryParam("q") String queryString, @QueryParam("province") String province) {
		SearchParams searchParams = new SearchParams();
		searchParams.setQueryString(queryString);
		return companyGroupService.find(searchParams);
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/merge")
	public Message<CompanyGroup> merge(CompanyGroup entity) {
		try {
			return companyGroupService.merge(entity);
		} catch (Exception ex) {
			return new Message<CompanyGroup>(Message.CODE_GENERIC_ERROR, i18nService.getMessage("companyGroup.error.merge"), entity);
		}
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/remove/{id}")
	public Message<CompanyGroup> remove(@PathParam("id") Long id) {
		try {
			return companyGroupService.remove(id);
		} catch (Exception ex) {
			return new Message<CompanyGroup>(Message.CODE_GENERIC_ERROR, i18nService.getMessage("companyGroup.error.remove"));
		}
	}
}
