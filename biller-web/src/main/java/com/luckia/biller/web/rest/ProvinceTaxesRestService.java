package com.luckia.biller.web.rest;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.luckia.biller.core.model.ProvinceTaxes;
import com.luckia.biller.core.model.common.SearchParams;
import com.luckia.biller.core.model.common.SearchResults;
import com.luckia.biller.core.services.entities.ProvinceTaxesService;

@Path("/provinceTaxes")
public class ProvinceTaxesRestService {

	@Inject
	private ProvinceTaxesService provinceTaxesService;

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/find")
	public SearchResults<ProvinceTaxes> find(@QueryParam("p") Integer currentPage, @QueryParam("n") Integer itemsPerPage, @QueryParam("q") String queryString) {
		SearchParams params = new SearchParams(currentPage, itemsPerPage, queryString);
		return provinceTaxesService.find(params);
	}
}
