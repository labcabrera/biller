package com.luckia.biller.core.services;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;

import com.google.inject.Singleton;
import com.luckia.biller.core.model.User;

@Singleton
public class SecurityService {

	private final ThreadLocal<User> users;

	@Inject
	private Provider<EntityManager> entityManagerProvider;

	public SecurityService() {
		users = new ThreadLocal<>();
	}

	public void setCurrentUser(User user) {
		users.set(user);
	}

	public User getCurrentUser() {
		if (users.get() != null) {
			return users.get();
		}
		try {
			return entityManagerProvider.get().createNamedQuery("User.selectByAlias", User.class).setParameter("alias", "admin").getSingleResult();
		} catch (NoResultException ex) {
			throw new RuntimeException("Error al obtener el usuario activo", ex);
		}
	}
}
