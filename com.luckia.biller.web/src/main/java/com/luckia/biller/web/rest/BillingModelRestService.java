package com.luckia.biller.web.rest;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.persistence.EntityManager;
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
import com.luckia.biller.core.model.BillingModel;
import com.luckia.biller.core.model.Rappel;
import com.luckia.biller.core.model.common.Message;
import com.luckia.biller.core.model.common.SearchParams;
import com.luckia.biller.core.model.common.SearchResults;
import com.luckia.biller.core.services.entities.BillingModelEntityService;

@Path("/models")
public class BillingModelRestService {

	private static final Logger LOG = LoggerFactory.getLogger(BillingModelRestService.class);

	@Inject
	private BillingModelEntityService billingModelService;
	@Inject
	private Provider<EntityManager> entityManagerProvider;
	@Inject
	private I18nService i18nService;

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("id/{id}")
	public BillingModel findById(@PathParam("id") Long primaryKey) {
		return billingModelService.findById(primaryKey);
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/find")
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

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/remove/{id}")
	public Message<BillingModel> remove(@PathParam("id") Long id) {
		try {
			return billingModelService.remove(id);
		} catch (Exception ex) {
			LOG.error("Error al eliminar el grupo", ex);
			return new Message<BillingModel>(Message.CODE_GENERIC_ERROR, i18nService.getMessage("companyGroup.error.remove"));
		}
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
