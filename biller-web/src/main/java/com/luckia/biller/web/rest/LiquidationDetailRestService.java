package com.luckia.biller.web.rest;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.luckia.biller.core.model.LiquidationDetail;
import com.luckia.biller.core.model.common.SearchParams;
import com.luckia.biller.core.model.common.SearchResults;
import com.luckia.biller.core.services.entities.LiquidationDetailEntityService;

@Path("adjustments/liquidation")
@Produces(MediaType.APPLICATION_JSON)
public class LiquidationDetailRestService {

	@Inject
	private LiquidationDetailEntityService entityService;

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/find")
	public SearchResults<LiquidationDetail> find(@QueryParam("n") Integer maxResults,
			@QueryParam("p") Integer page, @QueryParam("q") String queryString) {
		SearchParams params = new SearchParams();
		params.setItemsPerPage(maxResults);
		params.setCurrentPage(page);
		params.setQueryString(queryString);
		SearchResults<LiquidationDetail> results = entityService.find(params);
		return results;
	}

}
