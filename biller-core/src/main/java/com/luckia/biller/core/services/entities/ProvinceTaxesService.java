package com.luckia.biller.core.services.entities;

import java.math.BigDecimal;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.luckia.biller.core.model.AbstractBill;
import com.luckia.biller.core.model.Bill;
import com.luckia.biller.core.model.Province;
import com.luckia.biller.core.model.ProvinceTaxes;

/**
 * Servicio encargado de obtener la tasa de juego asociado a una factura. En teoria este valor dependera de la comunidad autonoma, aunque de
 * momento aplicamos el 10% para todas las facturas.
 */
public class ProvinceTaxesService extends EntityService<ProvinceTaxes> {

	private static final Logger LOG = LoggerFactory.getLogger(ProvinceTaxesService.class);

	// TODO eliminar valor por defecto
	private static final BigDecimal DEFAULT_FEES_VALUE = new BigDecimal("10.00");
	private static final BigDecimal DEFAULT_VAT = new BigDecimal("21.00");

	@Inject
	private Provider<EntityManager> entityManagerProvider;

	public BigDecimal getGameFeesPercent(Bill bill) {
		ProvinceTaxes entity = resolveTaxes(bill);
		if (entity != null && entity.getFeesPercent() != null) {
			return entity.getFeesPercent();
		} else {
			return DEFAULT_FEES_VALUE;
		}
	}

	public BigDecimal getVatPercent(Bill bill) {
		ProvinceTaxes entity = resolveTaxes(bill);
		if (entity != null && entity.getVatPercent() != null) {
			return entity.getVatPercent();
		} else {
			return DEFAULT_VAT;
		}
	}

	private ProvinceTaxes resolveTaxes(AbstractBill bill) {
		ProvinceTaxes result = null;
		Province province = bill.getSender().getAddress() != null ? bill.getSender().getAddress().getProvince() : null;
		if (province != null) {
			EntityManager entityManager = entityManagerProvider.get();
			TypedQuery<ProvinceTaxes> query = entityManager.createNamedQuery(ProvinceTaxes.QUERY_SELECT_BY_PROVINCE, ProvinceTaxes.class);
			query.setParameter("province", province);
			List<ProvinceTaxes> list = query.getResultList();
			if (!list.isEmpty()) {
				result = list.iterator().next();
			}
		}
		if (result == null) {
			LOG.warn("No se ha podido resolver la informacion de impuestos asociada a la factura: {} (provincia: {}, emisor: {})", bill, province, bill.getSender());
		}
		return result;
	}

	@Override
	protected Class<ProvinceTaxes> getEntityClass() {
		return ProvinceTaxes.class;
	}
}
