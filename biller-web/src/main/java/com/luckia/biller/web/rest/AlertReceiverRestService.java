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
import com.luckia.biller.core.model.AlertReceiver;
import com.luckia.biller.core.model.common.Message;
import com.luckia.biller.core.model.common.SearchParams;
import com.luckia.biller.core.model.common.SearchResults;
import com.luckia.biller.core.services.entities.AlertReceiverEntityService;

@Path("/alert-receivers")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AlertReceiverRestService {

	private static final Logger LOG = LoggerFactory
			.getLogger(AlertReceiverRestService.class);

	@Inject
	private AlertReceiverEntityService alertReceiverService;
	@Inject
	private I18nService i18nService;

	@GET
	@Path("id/{id}")
	public AlertReceiver findById(@PathParam("id") String primaryKey) {
		return alertReceiverService.findById(primaryKey);
	}

	@GET
	@Path("/find")
	public SearchResults<AlertReceiver> find(@QueryParam("p") Integer page,
			@QueryParam("n") Integer itemsPerPage, @QueryParam("q") String queryString) {
		SearchParams searchParams = new SearchParams();
		searchParams.setItemsPerPage(itemsPerPage);
		searchParams.setCurrentPage(page);
		searchParams.setQueryString(queryString);
		return alertReceiverService.find(searchParams);
	}

	@POST
	@Path("/merge")
	public Message<AlertReceiver> merge(AlertReceiver entity) {
		try {
			return alertReceiverService.merge(entity);
		}
		catch (Exception ex) {
			LOG.error("Merge error", ex);
			return new Message<AlertReceiver>(Message.CODE_GENERIC_ERROR,
					i18nService.getMessage("alertReceiver.error.merge"));
		}
	}

	@POST
	@Path("/remove/{id}")
	public Message<AlertReceiver> remove(@PathParam("id") String id) {
		try {
			return alertReceiverService.remove(id);
		}
		catch (Exception ex) {
			LOG.error("Error al eliminar el centro de coste", ex);
			return new Message<AlertReceiver>(Message.CODE_GENERIC_ERROR,
					i18nService.getMessage("alertReceiver.error.remove"));
		}
	}
}
