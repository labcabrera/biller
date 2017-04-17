package com.luckia.biller.deploy.patch;

import java.math.BigDecimal;
import java.math.RoundingMode;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.eclipse.persistence.config.QueryHints;
import org.eclipse.persistence.queries.ScrollableCursor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.persist.PersistService;
import com.luckia.biller.core.BillerModule;
import com.luckia.biller.core.common.MathUtils;
import com.luckia.biller.core.model.Liquidation;

/**
 * Actualiza el modelo en aquellas liquidaciones que carecen de el.
 */
public class PatchUpdateLiquidationModel {

	private static final Logger LOG = LoggerFactory
			.getLogger(PatchUpdateLiquidationModel.class);

	public static void main(String[] args) {
		Injector injector = Guice.createInjector(new BillerModule());
		injector.getInstance(PersistService.class).start();
		EntityManager entityManager = injector.getProvider(EntityManager.class).get();
		Query query = entityManager.createQuery(
				"select e from Liquidation e where e.model = null order by e.billDate");
		query.setHint(QueryHints.SCROLLABLE_CURSOR, true);
		ScrollableCursor cursor = (ScrollableCursor) query.getSingleResult();
		Long totalCount = 0L;
		Long updateCount = 0L;
		Long t0 = System.currentTimeMillis();
		entityManager.getTransaction().begin();
		while (cursor.hasNext()) {
			totalCount++;
			Liquidation liquidation = (Liquidation) cursor.next();
			if (liquidation.getModel() == null) {
				LOG.info("Actualizando liquidacion {}", liquidation);
				if (!liquidation.getBills().isEmpty()) {
					updateCount++;
					liquidation.setModel(
							liquidation.getBills().iterator().next().getModel());
					entityManager.merge(liquidation);
					if (totalCount % 25 == 0) {
						entityManager.flush();
					}
				}
				else {
					LOG.warn("Liquidacion {} sin facturas", liquidation);
				}
			}
		}
		entityManager.getTransaction().commit();
		cursor.close();
		LOG.debug("Liquidaciones totales: {}", totalCount);
		LOG.debug("Liquidaciones afecatadas: {}", updateCount);
		LOG.debug("Time: {} sg", new BigDecimal(System.currentTimeMillis() - t0)
				.divide(MathUtils.THOUSAND, 2, RoundingMode.HALF_EVEN));
	}
}
