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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.luckia.biller.core.i18n.I18nService;
import com.luckia.biller.core.model.Company;
import com.luckia.biller.core.model.common.Message;
import com.luckia.biller.core.model.common.SearchParams;
import com.luckia.biller.core.model.common.SearchResults;
import com.luckia.biller.core.services.entities.CompanyEntityService;

@Path("rest/companies")
public class CompanyRestService {

	private static final Logger LOG = LoggerFactory.getLogger(CompanyRestService.class);

	@Inject
	private CompanyEntityService companyService;
	@Inject
	private I18nService i18nService;

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("id/{id}")
	public Company findById(@PathParam("id") Long primaryKey) {
		return companyService.findById(primaryKey);
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/find")
	public SearchResults<Company> find(@QueryParam("p") Integer page, @QueryParam("n") Integer itemsPerPage, @QueryParam("q") String queryString) {
		SearchParams searchParams = new SearchParams();
		searchParams.setItemsPerPage(itemsPerPage);
		searchParams.setCurrentPage(page);
		searchParams.setQueryString(queryString);
		return companyService.find(searchParams);
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/merge")
	public Message<Company> merge(Company entity) {
		try {
			return companyService.merge(entity);
		} catch (Exception ex) {
			LOG.error("Error al actualizar la empresa", ex);
			return new Message<Company>(Message.CODE_GENERIC_ERROR, i18nService.getMessage("company.error.merge"));
		}
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/remove/{id}")
	public Message<Company> remove(@PathParam("id") Long id) {
		try {
			return companyService.remove(id);
		} catch (Exception ex) {
			LOG.error("Error al eliminar la empresa", ex);
			return new Message<Company>(Message.CODE_GENERIC_ERROR, i18nService.getMessage("company.error.remove"));
		}
	}
}
