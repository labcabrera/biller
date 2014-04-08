package com.luckia.biller.web.rest;

import java.math.BigDecimal;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.luckia.biller.core.model.RappelStoreBonus;
import com.luckia.biller.core.model.common.Message;
import com.luckia.biller.core.model.common.SearchParams;
import com.luckia.biller.core.model.common.SearchResults;
import com.luckia.biller.core.services.bills.RappelStoreProcessor;
import com.luckia.biller.core.services.entities.RapelStoreBonusEntityService;

@Path("rest/rappel/stores")
public class RappelStoreRestService {

	@Inject
	private RapelStoreBonusEntityService entityService;
	@Inject
	private RappelStoreProcessor rappelStoreProcessor;

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/id/{id}")
	public RappelStoreBonus findById(@PathParam("id") String primaryKey) {
		return entityService.findById(primaryKey);
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/find")
	public SearchResults<RappelStoreBonus> find(@QueryParam("n") Integer maxResults, @QueryParam("p") Integer page, @QueryParam("q") String queryString) {
		SearchParams params = new SearchParams();
		params.setItemsPerPage(maxResults);
		params.setCurrentPage(page);
		params.setQueryString(queryString);
		return entityService.find(params);
	}

	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/confirm/{id}")
	public Message<RappelStoreBonus> confirm(@PathParam("id") String primaryKey) {
		try {
			RappelStoreBonus bonus = entityService.findById(primaryKey);
			rappelStoreProcessor.confirm(bonus);
			return new Message<>(Message.CODE_SUCCESS, "Rappel confirmado", bonus);
		} catch (Exception e) {
			return new Message<>(Message.CODE_GENERIC_ERROR, "Error al aceptar el rappel");
		}
	}

	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/prorata")
	public Message<RappelStoreBonus> applyProrata(@PathParam("id") String primaryKey, BigDecimal prorata) {
		try {
			RappelStoreBonus bonus = entityService.findById(primaryKey);
			rappelStoreProcessor.applyProrata(bonus, prorata);
			return new Message<>(Message.CODE_SUCCESS, "Aplicado prorateo de rappel", bonus);
		} catch (Exception e) {
			return new Message<>(Message.CODE_GENERIC_ERROR, "Error al aceptar el prorateo de rappel");
		}
	}
}
