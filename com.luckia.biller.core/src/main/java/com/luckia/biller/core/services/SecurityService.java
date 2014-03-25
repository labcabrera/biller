package com.luckia.biller.core.services;

import javax.inject.Inject;

import com.luckia.biller.core.jpa.EntityManagerProvider;
import com.luckia.biller.core.model.User;

public class SecurityService {

	@Inject
	private EntityManagerProvider entityManagerProvider;

	// TODO dummy
	public User getCurrentUser() {
		return entityManagerProvider.get().createNamedQuery("User.selectByName", User.class).setParameter("name", "admin").getSingleResult();
	}
}
