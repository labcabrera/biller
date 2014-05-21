package com.luckia.biller.core.services.bills.impl;

import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import com.luckia.biller.core.jpa.EntityManagerProvider;
import com.luckia.biller.core.model.Company;
import com.luckia.biller.core.model.LegalEntity;

/**
 * Componente encargado de resolver la entidad legal receptora de una liquidacion (Egasa Hattrick)
 */
public class LiquidationReceiverProvider {

	@Inject
	private EntityManagerProvider entityManagerProvider;

	// TODO estaria mejor leerlo de configuracion
	public LegalEntity getReceiver() {
		EntityManager entityManager = entityManagerProvider.get();
		TypedQuery<Company> queryEgasa = entityManager.createQuery("select e from Company e where e.name = :name", Company.class);
		List<Company> list = queryEgasa.setParameter("name", "'EGASA HATTRICK S.A.'").getResultList();
		return list.iterator().next();
	}
}
