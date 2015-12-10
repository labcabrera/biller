package com.luckia.biller.web.rest;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.luckia.biller.core.model.BillLiquidationDetail;
import com.luckia.biller.core.model.common.SearchParams;
import com.luckia.biller.core.model.common.SearchResults;
import com.luckia.biller.core.services.entities.BillLiquidationDetailEntityService;

@Path("adjustments/store")
@Produces(MediaType.APPLICATION_JSON)
public class BillLiquidationDetailRestService {

	@Inject
	private BillLiquidationDetailEntityService entityService;

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/find")
	public SearchResults<BillLiquidationDetail> find(@QueryParam("n") Integer maxResults, @QueryParam("p") Integer page, @QueryParam("q") String queryString) {
		SearchParams params = new SearchParams();
		params.setItemsPerPage(maxResults);
		params.setCurrentPage(page);
		params.setQueryString(queryString);
		SearchResults<BillLiquidationDetail> results = entityService.find(params);
		return results;
	}

}
