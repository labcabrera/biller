package com.luckia.biller.core.services;

import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.persistence.EntityManager;

import com.luckia.biller.core.model.HasState;
import com.luckia.biller.core.model.State;
import com.luckia.biller.core.model.StateDefinition;

/**
 * Servicio encargado de realizar los cambios de estado de las entidades {@link com.luckia.biller.core.model.HasState}
 */
public class StateMachineService {

	@Inject
	private Provider<EntityManager> entityManagerProvider;

	public void createTransition(HasState hasState, String stateDefinitionId) {
		EntityManager entityManager = entityManagerProvider.get();
		Boolean currentTransaction = entityManager.getTransaction().isActive();
		if (!currentTransaction) {
			entityManager.getTransaction().begin();
		}
		StateDefinition stateDefinition = entityManager.find(StateDefinition.class, stateDefinitionId);
		Date now = Calendar.getInstance().getTime();
		State state = new State();
		state.setId(UUID.randomUUID().toString());
		state.setEntered(now);
		state.setStateDefinition(stateDefinition);
		hasState.setCurrentState(state);
		entityManager.merge(hasState);
		if (!currentTransaction) {
			entityManager.getTransaction().commit();
		}
	}
}
