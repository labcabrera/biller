/*******************************************************************************
 * Copyright (c) 2004, 2012 Kotasoft S.L.
 * All rights reserved. This program and the accompanying materials
 * may only be used prior written consent of Kotasoft S.L.
 ******************************************************************************/
package com.luckia.biller.deploy.fedders;

import java.io.InputStream;

import javax.inject.Inject;
import javax.persistence.EntityManager;

import com.luckia.biller.core.jpa.EntityManagerProvider;
import com.luckia.biller.core.model.Bill;
import com.luckia.biller.core.model.CommonState;
import com.luckia.biller.core.model.StateDefinition;

public class StateDefinitionFeeder implements Feeder<StateDefinition> {

	@Inject
	private EntityManagerProvider entityManagerProvider;

	@Override
	public void loadEntities(InputStream source) {
		EntityManager entityManager = entityManagerProvider.get();
		for (CommonState state : CommonState.values()) {
			entityManager.persist(new StateDefinition(state.name(), Bill.class, state.desc()));
		}
	}
}
