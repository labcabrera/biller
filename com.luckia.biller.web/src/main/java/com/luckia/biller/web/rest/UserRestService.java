package com.luckia.biller.web.rest;

import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;

import com.luckia.biller.core.jpa.EntityManagerProvider;
import com.luckia.biller.core.model.User;
import com.luckia.biller.core.services.entities.UserEntityService;

@Path("rest/users")
public class UserRestService {

	@Inject
	private UserEntityService userEntityService;
	@Inject
	private EntityManagerProvider entityManagerProvider;

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
	@Path("/mail/{mail}")
	public User findByMail(@PathParam("mail") String mail) {
		EntityManager entityManager = entityManagerProvider.get();
		entityManager.clear();
		List<User> values = entityManager.createQuery("select u from User u where u.email = :mail", User.class).setParameter("mail", mail).getResultList();
		return values.isEmpty() ? null : values.iterator().next();
	}

	// TODO establecer la estructura del token de autenticacion
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/login")
	public Object login(@QueryParam("name") String name, @QueryParam("password") String password, @Context SecurityContext securityContext) {
		return "true";
	}
}
