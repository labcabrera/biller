package com.luckia.biller.core.services;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;

import com.luckia.biller.core.model.User;

public class SecurityService {

	@Inject
	private Provider<EntityManager> entityManagerProvider;

	// TODO dummy
	public User getCurrentUser() {
		try {
			return entityManagerProvider.get().createNamedQuery("User.selectByName", User.class).setParameter("name", "admin").getSingleResult();
		} catch (NoResultException ex) {
			throw new RuntimeException("Error al obtener el usuario activo", ex);
		}
	}
}
