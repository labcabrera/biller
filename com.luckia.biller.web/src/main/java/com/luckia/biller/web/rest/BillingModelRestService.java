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

import com.luckia.biller.core.ClearCache;
import com.luckia.biller.core.jpa.EntityManagerProvider;
import com.luckia.biller.core.model.BillingModel;
import com.luckia.biller.core.model.Rappel;
import com.luckia.biller.core.model.common.Message;
import com.luckia.biller.core.model.common.SearchParams;
import com.luckia.biller.core.model.common.SearchResults;
import com.luckia.biller.core.services.entities.BillingModelEntityService;

@Path("rest/models")
public class BillingModelRestService {

	@Inject
	private BillingModelEntityService billingModelService;
	@Inject
	private EntityManagerProvider entityManagerProvider;

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("id/{id}")
	@ClearCache
	public BillingModel findById(@PathParam("id") Long primaryKey) {
		return billingModelService.findById(primaryKey);
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/find")
	@ClearCache
	public SearchResults<BillingModel> find(@QueryParam("n") Integer maxResults, @QueryParam("p") Integer page, @QueryParam("q") String queryString) {
		SearchParams params = new SearchParams();
		params.setCurrentPage(page);
		params.setItemsPerPage(maxResults);
		params.setQueryString(queryString);
		return billingModelService.find(params);
	}

	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("/merge")
	public Message<BillingModel> merge(BillingModel model) {
		return billingModelService.merge(model);
	}

	/**
	 * Busca un tramo de rappel a partir de su identificador
	 * 
	 * @param primaryKey
	 * @return
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/rappel/id/{id}")
	public Rappel findRappelById(@PathParam("id") Long primaryKey) {
		return entityManagerProvider.get().find(Rappel.class, primaryKey);
	}

	/**
	 * Crea o actualiza un tramo de rappel asociado a un modelo de facturacion
	 * 
	 * @param entity
	 * @return
	 */
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("/rappel/merge")
	public Message<BillingModel> mergeRappelDetail(Rappel entity) {
		return billingModelService.mergeRappelDetail(entity);
	}

	/**
	 * Elimina un tramo de rappel asociado a un modelo de facturacion
	 * 
	 * @param entity
	 * @return
	 */
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("/rappel/remove/{id}")
	public Message<BillingModel> removeRappelDetail(@PathParam("id") Long primaryKey) {
		return billingModelService.removeRappelDetail(primaryKey);
	}
}
