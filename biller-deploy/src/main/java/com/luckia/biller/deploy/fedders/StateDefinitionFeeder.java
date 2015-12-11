package com.luckia.biller.deploy.fedders;

import java.io.InputStream;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.persistence.EntityManager;

import com.luckia.biller.core.model.Bill;
import com.luckia.biller.core.model.CommonState;
import com.luckia.biller.core.model.StateDefinition;

public class StateDefinitionFeeder implements Feeder<StateDefinition> {

	@Inject
	private Provider<EntityManager> entityManagerProvider;

	@Override
	public void loadEntities(InputStream source) {
		EntityManager entityManager = entityManagerProvider.get();
		for (CommonState state : CommonState.values()) {
			entityManager.persist(new StateDefinition(state.name(), Bill.class, state.name()));
		}
	}
}
