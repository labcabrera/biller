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
import com.luckia.biller.core.model.Owner;
import com.luckia.biller.core.model.common.Message;
import com.luckia.biller.core.model.common.SearchParams;
import com.luckia.biller.core.model.common.SearchResults;
import com.luckia.biller.core.services.entities.OwnerEntityService;

@Path("rest/owners")
public class OwnerRestService {

	private static final Logger LOG = LoggerFactory.getLogger(OwnerRestService.class);

	@Inject
	private OwnerEntityService ownerService;
	@Inject
	private I18nService i18nService;

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("id/{id}")
	public Owner findById(@PathParam("id") Long primaryKey) {
		return ownerService.findById(primaryKey);
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/find")
	public SearchResults<Owner> find(@QueryParam("n") Integer maxResults, @QueryParam("p") Integer page, @QueryParam("q") String queryString) {
		SearchParams params = new SearchParams();
		params.setItemsPerPage(maxResults);
		params.setCurrentPage(page);
		params.setQueryString(queryString);
		return ownerService.find(params);
	}

	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("/merge")
	public Message<Owner> merge(Owner entity) {
		try {
			return ownerService.merge(entity);
		} catch (Exception ex) {
			LOG.error("Error al actualizar el titular", ex);
			return new Message<Owner>(Message.CODE_GENERIC_ERROR, i18nService.getMessage("owner.error.merge"));
		}
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/remove/{id}")
	public Message<Owner> remove(@PathParam("id") Long id) {
		try {
			return ownerService.remove(id);
		} catch (Exception ex) {
			LOG.error("Error al eliminar el titular", ex);
			return new Message<Owner>(Message.CODE_GENERIC_ERROR, i18nService.getMessage("owner.error.remove"));
		}
	}
}
