package com.luckia.biller.deploy.patch;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;

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
import com.luckia.biller.core.model.Bill;
import com.luckia.biller.core.model.Liquidation;
import com.luckia.biller.core.model.LiquidationDetail;
import com.luckia.biller.core.model.LiquidationResults;

/**
 * Patch para la actualizacion del refactor en el modelo de liquidaciones.
 */
public class PatchUpgradeLiquidationResults {

	private static final Logger LOG = LoggerFactory.getLogger(PatchUpdateLiquidationModel.class);

	public static void main(String[] args) {
		Injector injector = Guice.createInjector(new BillerModule());
		injector.getInstance(PersistService.class).start();
		EntityManager entityManager = injector.getProvider(EntityManager.class).get();

		Query query = entityManager.createQuery("select e from Liquidation e where e.id in :ids order by e.billDate");
		query.setParameter("ids", Arrays.asList("fcfc58a4-4f5f-4ffe-8621-80c364475ef0"));
		query.setMaxResults(10);

		query.setHint(QueryHints.SCROLLABLE_CURSOR, true);
		ScrollableCursor cursor = (ScrollableCursor) query.getSingleResult();
		Long totalCount = 0L;
		Long t0 = System.currentTimeMillis();

		entityManager.getTransaction().begin();
		while (cursor.hasNext()) {
			totalCount++;
			Liquidation liquidation = (Liquidation) cursor.next();
			LOG.info("Actualizando liquidacion {}", liquidation);
			liquidation.setModelVersion("1.3.0");
			LiquidationResults liquidationResults = liquidation.getLiquidationResults();
			liquidationResults.setEffectiveLiquidationAmount(liquidationResults.getReceiverAmount());
			liquidationResults.setStoreManualOuterAmount(BigDecimal.ZERO);
			liquidationResults.setLiquidationManualOuterAmount(BigDecimal.ZERO);
			liquidationResults.setCashStoreEffectiveAmount(liquidationResults.getCashStoreAmount());
			liquidationResults.setTotalAmount(liquidationResults.getCashStoreAmount().subtract(liquidationResults.getEffectiveLiquidationAmount()));
			for (LiquidationDetail detail : liquidation.getDetails()) {
				detail.setNetValue(detail.getValue());
				detail.setVatValue(BigDecimal.ZERO);
				entityManager.merge(detail);
			}
			for (Bill bill : liquidation.getBills()) {
				bill.setLiquidationTotalNetAmount(bill.getLiquidationTotalAmount());
				bill.setLiquidationTotalVat(BigDecimal.ZERO);
			}
			entityManager.merge(liquidation);
			if (totalCount % 25 == 0) {
				entityManager.flush();
			}
		}
		entityManager.getTransaction().commit();
		cursor.close();
		LOG.debug("Liquidaciones totales: {}", totalCount);
		LOG.debug("Time: {} sg", new BigDecimal(System.currentTimeMillis() - t0).divide(MathUtils.THOUSAND, 2, RoundingMode.HALF_EVEN));
	}
}
