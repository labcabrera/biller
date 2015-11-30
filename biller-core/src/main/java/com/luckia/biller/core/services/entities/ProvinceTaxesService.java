package com.luckia.biller.core.services.entities;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.persist.Transactional;
import com.luckia.biller.core.common.NoAvailableDataException;
import com.luckia.biller.core.common.RegisterActivity;
import com.luckia.biller.core.model.AbstractBill;
import com.luckia.biller.core.model.Bill;
import com.luckia.biller.core.model.Liquidation;
import com.luckia.biller.core.model.Province;
import com.luckia.biller.core.model.ProvinceTaxes;
import com.luckia.biller.core.model.UserActivityType;
import com.luckia.biller.core.model.common.Message;

/**
 * Servicio encargado de obtener la tasa de juego asociado a una factura. En teoria este valor dependera de la comunidad autonoma, aunque de momento aplicamos el 10% para todas las
 * facturas.
 */
public class ProvinceTaxesService extends EntityService<ProvinceTaxes> {

	private static final Logger LOG = LoggerFactory.getLogger(ProvinceTaxesService.class);

	@Inject
	private Provider<EntityManager> entityManagerProvider;

	@Override
	@Transactional
	@RegisterActivity(type = UserActivityType.PROVICE_TAX_MERGE)
	public Message<ProvinceTaxes> merge(ProvinceTaxes entity) {
		try {
			EntityManager entityManager = entityManagerProvider.get();
			ProvinceTaxes current = entityManager.find(ProvinceTaxes.class, entity.getId());
			current.setFeesPercent(entity.getFeesPercent() != null ? entity.getFeesPercent().setScale(2, RoundingMode.HALF_EVEN) : BigDecimal.ZERO);
			current.setVatPercent(entity.getVatPercent() != null ? entity.getVatPercent().setScale(2, RoundingMode.HALF_EVEN) : BigDecimal.ZERO);
			entityManager.merge(current);
			return new Message<ProvinceTaxes>().withMessage("provinceTaxes.merge.success").withPayload(current);
		} catch (Exception ex) {
			LOG.error("Merge error", ex);
			return new Message<ProvinceTaxes>().withCode(Message.CODE_GENERIC_ERROR).withMessage("provinceTaxes.merge.error");

		}
	}

	public BigDecimal getGameFeesPercent(Bill bill) {
		ProvinceTaxes entity = resolveTaxes(bill);
		if (entity != null && entity.getFeesPercent() != null) {
			return entity.getFeesPercent();
		} else {
			throw new NoAvailableDataException("Cant recover games fees percent from bill " + bill.getId());
		}
	}

	public BigDecimal getVatPercent(Bill bill) {
		ProvinceTaxes entity = resolveTaxes(bill);
		if (entity != null && entity.getVatPercent() != null) {
			return entity.getVatPercent();
		} else {
			throw new NoAvailableDataException("Cant recover vat percent from bill " + bill.getId());
		}
	}

	public BigDecimal getVatPercent(Liquidation liquidation) {
		Validate.isTrue(!liquidation.getBills().isEmpty(), "Liquidation has no bills. Cant resolve province vat percent");
		return getVatPercent(liquidation.getBills().iterator().next());
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
	protected void buildOrderCriteria(CriteriaQuery<ProvinceTaxes> criteria, CriteriaBuilder builder, Root<ProvinceTaxes> root) {
		criteria.orderBy(builder.asc(root.<Province> get("province").<String> get("name")));
	}

	@Override
	protected Class<ProvinceTaxes> getEntityClass() {
		return ProvinceTaxes.class;
	}
}
