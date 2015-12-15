package com.luckia.biller.web.rest;

import java.util.List;

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

import com.luckia.biller.core.model.User;
import com.luckia.biller.core.model.UserRole;
import com.luckia.biller.core.model.common.Message;
import com.luckia.biller.core.model.common.SearchParams;
import com.luckia.biller.core.model.common.SearchResults;
import com.luckia.biller.core.services.entities.UserEntityService;

@Path("/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserRestService {

	@Inject
	private UserEntityService userEntityService;
	@Inject
	private Provider<EntityManager> entityManagerProvider;

	@GET
	@Path("/id/{id}")
	public User findById(@PathParam("id") Long primaryKey) {
		return userEntityService.findById(primaryKey);
	}

	@GET
	@Path("/name/{name}")
	public User findByName(@PathParam("name") String name) {
		return userEntityService.findByName(name);
	}

	@GET
	@Path("/find")
	public SearchResults<User> find(@QueryParam("n") Integer maxResults, @QueryParam("p") Integer page, @QueryParam("q") String queryString) {
		SearchParams params = new SearchParams();
		params.setItemsPerPage(maxResults);
		params.setCurrentPage(page);
		params.setQueryString(queryString);
		return userEntityService.find(params);
	}

	@POST
	@Path("/merge")
	public Message<User> merge(User user) {
		return userEntityService.merge(user);
	}

	@POST
	@Path("/remove/{id}")
	public Message<User> remove(@PathParam("id") Long id) {
		return userEntityService.remove(id);
	}

	@GET
	@Path("/mail/{mail}")
	public User findByMail(@PathParam("mail") String mail) {
		EntityManager entityManager = entityManagerProvider.get();
		List<User> values = entityManager.createQuery("select u from User u where u.email = :mail", User.class).setParameter("mail", mail).getResultList();
		return values.isEmpty() ? null : values.iterator().next();
	}

	@GET
	@Path("/roles")
	public List<UserRole> roles() {
		EntityManager entityManager = entityManagerProvider.get();
		return entityManager.createQuery("select e from UserRole e order by e.code", UserRole.class).getResultList();

	}
}
