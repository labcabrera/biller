package com.luckia.biller.core.services.entities;

import com.luckia.biller.core.model.BillLiquidationDetail;

public class BillLiquidationDetailEntityService
		extends EntityService<BillLiquidationDetail> {

	@Override
	protected Class<BillLiquidationDetail> getEntityClass() {
		return BillLiquidationDetail.class;
	}
}
