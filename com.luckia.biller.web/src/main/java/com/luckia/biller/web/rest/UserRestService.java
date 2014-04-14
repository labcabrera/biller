package com.luckia.biller.web.rest;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;

import org.apache.commons.codec.binary.Base64;

import com.luckia.biller.core.jpa.EntityManagerProvider;
import com.luckia.biller.core.model.User;
import com.luckia.biller.core.model.common.Message;
import com.luckia.biller.core.services.entities.UserEntityService;

@Path("rest/users")
public class UserRestService {
	
	public static void main(String[] args) {
		System.out.println(new UserRestService().getBase64Digest("luckia"));
	}

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
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/login")
	public Message<User> login(User user, @Context SecurityContext securityContext) {
		EntityManager entityManager = entityManagerProvider.get();
		TypedQuery<User> query = entityManager.createQuery("select e from User e where e.name = :name", User.class).setParameter("name", user.getName());
		List<User> list = query.getResultList();
		if (list.isEmpty()) {
			return new Message<>(Message.CODE_GENERIC_ERROR, "Usuario no encontrado");
		} else {
			String userDigest = user.getPassword() != null ? getBase64Digest(user.getPassword()) : "";
			User target = list.iterator().next();
			if (userDigest.equals(target.getPasswordDigest())) {
				entityManager.detach(target);
				target.setPasswordDigest(null);
				return new Message<>(Message.CODE_SUCCESS, "Success", target);
			} else {
				return new Message<>(Message.CODE_GENERIC_ERROR, "Password no válida");
			}
		}
	}

	private String getBase64Digest(String message) {
		try {
			MessageDigest messageDigest = MessageDigest.getInstance("SHA-1");
			byte[] digest = messageDigest.digest(message.getBytes("UTF8"));
			return Base64.encodeBase64String(digest);
		} catch (NoSuchAlgorithmException | UnsupportedEncodingException ex) {
			throw new RuntimeException(ex);
		}
	}

}
