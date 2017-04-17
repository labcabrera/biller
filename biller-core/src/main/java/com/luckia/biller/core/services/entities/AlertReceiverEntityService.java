package com.luckia.biller.core.services.entities;

import javax.persistence.EntityManager;

import org.apache.commons.lang3.StringUtils;

import com.google.inject.persist.Transactional;
import com.luckia.biller.core.model.AlertReceiver;
import com.luckia.biller.core.model.common.Message;

public class AlertReceiverEntityService extends EntityService<AlertReceiver> {

	@Override
	@Transactional
	public Message<AlertReceiver> merge(AlertReceiver entity) {
		EntityManager entityManager = entityManagerProvider.get();
		AlertReceiver current;
		if (StringUtils.isNotBlank(entity.getId())) {
			current = entityManager.find(AlertReceiver.class, entity.getId());
			current.merge(entity);
			entityManager.merge(current);
		}
		else {
			current = new AlertReceiver();
			current.merge(entity);
			entityManager.persist(current);
		}
		return new Message<AlertReceiver>().withMessage("alertReceiver.merge.success")
				.withPayload(current);
	}

	@Override
	protected Class<AlertReceiver> getEntityClass() {
		return AlertReceiver.class;
	}
}
