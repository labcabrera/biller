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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.persist.Transactional;
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

	private static final Logger LOG = LoggerFactory.getLogger(UserRestService.class);

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
	@Transactional
	// TODO mover a un servicio para que la logica no este en la capa rest
	public Message<User> merge(User user) {
		Message<User> message = new Message<>();
		try {
			if (user.getPasswordDigest() != null) {
				// TODO
			}
			EntityManager entityManager = entityManagerProvider.get();
			if (user.getId() != null) {
				User current = entityManager.find(User.class, user.getId());
				current.merge(user);
				entityManager.merge(current);
				message.addInfo("user.merge.success").withPayload(current);
			} else {
				entityManager.persist(user);
				entityManager.flush();
				message.addInfo("user.insert.success").withPayload(user);
			}
		} catch (Exception ex) {
			LOG.error("User merge error");
			message.withCode(Message.CODE_GENERIC_ERROR).addError("user.merge.error");
		}
		return message;
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
		return entityManager.createQuery("select e from UserRole e order by e.name", UserRole.class).getResultList();

	}
}
