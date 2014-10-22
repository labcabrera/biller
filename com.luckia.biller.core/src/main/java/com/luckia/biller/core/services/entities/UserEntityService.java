package com.luckia.biller.core.services.entities;

import java.util.List;

import javax.persistence.EntityManager;

import com.luckia.biller.core.model.User;

public class UserEntityService extends EntityService<User> {

	public User findByName(String name) {
		EntityManager entityManager = entityManagerProvider.get();
		List<User> values = entityManager.createQuery("select u from User u where u.name = :name", User.class).setParameter("name", name).getResultList();
		return values.isEmpty() ? null : values.iterator().next();
	}

	@Override
	protected Class<User> getEntityClass() {
		return User.class;
	}
}
