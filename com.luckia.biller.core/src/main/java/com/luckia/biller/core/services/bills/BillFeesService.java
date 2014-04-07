package com.luckia.biller.core.services.bills;

import java.math.BigDecimal;
import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import com.luckia.biller.core.jpa.EntityManagerProvider;
import com.luckia.biller.core.model.Bill;
import com.luckia.biller.core.model.BillingProvinceFees;
import com.luckia.biller.core.model.Province;

/**
 * Servicio encargado de obtener la tasa de juego asociado a una factura. En teoria este valor dependera de la comunidad autonoma, aunque de
 * momento aplicamos el 10% para todas las facturas.
 */
public class BillFeesService {

	private static final BigDecimal DEFAULT_VALUE = new BigDecimal("10.00");

	@Inject
	private EntityManagerProvider entityManagerProvider;

	// TODO
	public BigDecimal getGameFeesPercent(Bill bill) {
		Province province = bill.getSender().getAddress() != null ? bill.getSender().getAddress().getProvince() : null;
		if (province != null) {
			EntityManager entityManager = entityManagerProvider.get();
			TypedQuery<BillingProvinceFees> query = entityManager.createQuery("select e from BillingProvinceFees e where e.province = :province", BillingProvinceFees.class);
			query.setParameter("province", province);
			List<BillingProvinceFees> list = query.getResultList();
			return list.isEmpty() ? DEFAULT_VALUE : list.iterator().next().getFeesPercent();
		} else {
			return DEFAULT_VALUE;
		}
	}
}
