package com.luckia.biller.web.rest;

import java.util.Iterator;

import javax.inject.Inject;
import javax.persistence.PersistenceException;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
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
import com.luckia.biller.core.model.Store;
import com.luckia.biller.core.model.common.Message;
import com.luckia.biller.core.model.common.SearchParams;
import com.luckia.biller.core.model.common.SearchResults;
import com.luckia.biller.core.services.entities.StoreEntityService;

@Path("/stores")
public class StoreRestService {

	private static final Logger LOG = LoggerFactory.getLogger(StoreRestService.class);

	@Inject
	private StoreEntityService entityService;
	@Inject
	private I18nService i18nService;

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/id/{id}")
	public Store findById(@PathParam("id") Long primaryKey) {
		Store result = entityService.findById(primaryKey);
		if (result != null && result.getOwner() == null) {
			LOG.warn("Missing owner in store {}", primaryKey);
		}
		return result;
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/find")
	public SearchResults<Store> find(@QueryParam("n") Integer maxResults, @QueryParam("p") Integer page, @QueryParam("q") String queryString) {
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
	public Message<Store> merge(Store entity) {
		try {
			return entityService.merge(entity);
		} catch (PersistenceException ex) {
			LOG.error("Error al actualizar el establecimiento", ex);
			Message<Store> message = new Message<>(Message.CODE_GENERIC_ERROR, i18nService.getMessage("store.error.merge"));
			if (ex.getCause() instanceof ConstraintViolationException) {
				ConstraintViolationException cve = (ConstraintViolationException) ex.getCause();
				for (Iterator<ConstraintViolation<?>> it = cve.getConstraintViolations().iterator(); it.hasNext();) {
					ConstraintViolation<? extends Object> v = it.next();
					System.err.println(v);
					System.err.println("==>>" + v.getMessage());
				}
			}
			return message;
		} catch (Exception ex) {
			LOG.error("Error al actualizar el establecimiento", ex);
			return new Message<>(Message.CODE_GENERIC_ERROR, i18nService.getMessage("store.error.merge"));
		}
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/remove/{id}")
	public Message<Store> remove(@PathParam("id") Long id) {
		try {
			return entityService.remove(id);
		} catch (Exception ex) {
			LOG.error("Error al eliminar el establecimiento", ex);
			return new Message<Store>(Message.CODE_GENERIC_ERROR, i18nService.getMessage("store.error.remove"));
		}
	}
}
