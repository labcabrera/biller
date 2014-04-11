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
import com.luckia.biller.core.model.TerminalRelation;
import com.luckia.biller.core.model.common.Message;
import com.luckia.biller.core.model.common.SearchParams;
import com.luckia.biller.core.model.common.SearchResults;
import com.luckia.biller.core.services.entities.TerminalRelationEntityService;

@Path("rest/terminals")
public class TerminalRelationRestService {

	private static final Logger LOG = LoggerFactory.getLogger(TerminalRelationRestService.class);

	@Inject
	private TerminalRelationEntityService entityService;
	@Inject
	private I18nService i18nService;

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/id/{id}")
	public TerminalRelation findById(@PathParam("id") Long primaryKey) {
		return entityService.findById(primaryKey);
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/find")
	public SearchResults<TerminalRelation> find(@QueryParam("n") Integer maxResults, @QueryParam("p") Integer page, @QueryParam("q") String queryString) {
		SearchParams params = new SearchParams();
		params.setItemsPerPage(maxResults);
		params.setCurrentPage(page);
		params.setQueryString(queryString);
		return entityService.find(params);
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/merge")
	public Message<TerminalRelation> merge(TerminalRelation entity) {
		try {
			return entityService.merge(entity);
		} catch (Exception ex) {
			LOG.error("Error al actualizar el terminal", ex);
			return new Message<>(Message.CODE_GENERIC_ERROR, i18nService.getMessage("terminal.error.merge"));
		}
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/remove/{id}")
	public Message<TerminalRelation> remove(@PathParam("id") Long id) {
		try {
			return entityService.remove(id);
		} catch (Exception ex) {
			LOG.error("Error al eliminar el terminal", ex);
			return new Message<>(Message.CODE_GENERIC_ERROR, i18nService.getMessage("terminal.error.remove"));
		}
	}
}
