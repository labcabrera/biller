package com.luckia.biller.core.services.bills.impl;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import com.luckia.biller.core.model.Company;
import com.luckia.biller.core.model.LegalEntity;

/**
 * Componente encargado de resolver la entidad legal receptora por defecto de una liquidacion (Egasa Hattrick) cuando no este indicada en el modelo de facturacion.
 */
public class LiquidationReceiverProvider {

	@Inject
	private Provider<EntityManager> entityManagerProvider;

	@Inject
	@Named("default-liquidation-receiver-name")
	private String defaultReceiverName;

	public LegalEntity getReceiver() {
		EntityManager entityManager = entityManagerProvider.get();
		TypedQuery<Company> queryEgasa = entityManager.createQuery("select e from Company e where e.name = :name", Company.class);
		List<Company> list = queryEgasa.setParameter("name", defaultReceiverName).getResultList();
		if (list.isEmpty()) {
			throw new RuntimeException(String.format("No se encuentra la empresa %s", defaultReceiverName));
		}
		return list.iterator().next();
	}
}
