package com.luckia.biller.core.scheduler.tasks;

import static org.apache.commons.lang3.time.DateFormatUtils.ISO_DATE_FORMAT;

import java.util.List;

import javax.inject.Provider;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.luckia.biller.core.model.CommonState;
import com.luckia.biller.core.model.Liquidation;
import com.luckia.biller.core.services.LiquidationMailService;
import com.luckia.biller.core.services.SettingsService;

/**
 * Tarea encargada de buscar las liquidaciones aceptadas y enviar el correo al destinatario
 */
public class SendLiquidationsTask implements Runnable {

	private static final Logger LOG = LoggerFactory.getLogger(SendLiquidationsTask.class);

	private final Provider<EntityManager> entityManagerProvider;
	private final LiquidationMailService liquidationMailService;
	private final SettingsService settingsService;

	public SendLiquidationsTask(Provider<EntityManager> entityManagerProvider, LiquidationMailService liquidationMailService, SettingsService settingsService) {
		this.entityManagerProvider = entityManagerProvider;
		this.liquidationMailService = liquidationMailService;
		this.settingsService = settingsService;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		try {
			EntityManager entityManager = entityManagerProvider.get();
			String qlString = "select e from Liquidation e where e.currentState.stateDefinition.id = :state";
			TypedQuery<Liquidation> query = entityManager.createQuery(qlString, Liquidation.class);
			query.setParameter("state", CommonState.Confirmed.name());
			List<Liquidation> liquidations = query.getResultList();
			if (!liquidations.isEmpty()) {
				LOG.info("Encontradas {} liquidaciones pendientes de ser enviadas por correo", liquidations.size());
				for (Liquidation liquidation : liquidations) {
					String emailAddress = resolveMailAddress(liquidation);
					liquidationMailService.sendEmail(liquidation, emailAddress, true);
					LOG.info("Enviada liquidacion de {}/{} a {}", liquidation.getSender().getName(), ISO_DATE_FORMAT.format(liquidation.getDateTo()), emailAddress);
				}
			}
		} catch (Exception ex) {
			LOG.error("Error durante el envio de correos", ex);
		}
	}

	private String resolveMailAddress(Liquidation liquidation) {
		Boolean useDefaultReceiver = settingsService.getMailSettings().getValue("useDefaultReceiver", Boolean.class);
		if (BooleanUtils.isTrue(useDefaultReceiver) || StringUtils.isEmpty(liquidation.getSender().getEmail())) {
			String defaultReceiver = settingsService.getMailSettings().getValue("defaultReceiver", String.class);
			return StringUtils.isEmpty(defaultReceiver) ? "pagos@luckia.es" : defaultReceiver;
		} else {
			return liquidation.getSender().getEmail();
		}
	}
}
