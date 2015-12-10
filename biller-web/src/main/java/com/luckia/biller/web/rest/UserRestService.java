package com.luckia.biller.web.rest;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.persistence.EntityManager;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.luckia.biller.core.model.User;
import com.luckia.biller.core.model.common.SearchParams;
import com.luckia.biller.core.model.common.SearchResults;
import com.luckia.biller.core.services.entities.UserEntityService;

@Path("/users")
public class UserRestService {

	@Inject
	private UserEntityService userEntityService;
	@Inject
	private Provider<EntityManager> entityManagerProvider;

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/id/{id}")
	public User findById(@PathParam("id") Long primaryKey) {
		return userEntityService.findById(primaryKey);
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/name/{name}")
	public User findByName(@PathParam("name") String name) {
		return userEntityService.findByName(name);
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/find")
	public SearchResults<User> find(@QueryParam("n") Integer maxResults, @QueryParam("p") Integer page, @QueryParam("q") String queryString) {
		SearchParams params = new SearchParams();
		params.setItemsPerPage(maxResults);
		params.setCurrentPage(page);
		params.setQueryString(queryString);
		return userEntityService.find(params);
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/mail/{mail}")
	public User findByMail(@PathParam("mail") String mail) {
		EntityManager entityManager = entityManagerProvider.get();
		entityManager.clear();
		List<User> values = entityManager.createQuery("select u from User u where u.email = :mail", User.class).setParameter("mail", mail).getResultList();
		return values.isEmpty() ? null : values.iterator().next();
	}
}
