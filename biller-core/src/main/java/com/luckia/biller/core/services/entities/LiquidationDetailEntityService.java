package com.luckia.biller.core.services.entities;

import com.luckia.biller.core.model.LiquidationDetail;

public class LiquidationDetailEntityService extends EntityService<LiquidationDetail> {

	@Override
	protected Class<LiquidationDetail> getEntityClass() {
		return LiquidationDetail.class;
	}
}
