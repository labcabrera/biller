package com.luckia.biller.core.model;

public enum UserActivityType {

	LIQUIDATION_CALCULATION,

	LIQUIDATION_APPROBATION,

	COST_CENTER_INSERT, COST_CENTER_UPDATE, COST_CENTER_DELETE,

	/** Modificacion de un ajuste manual de liquidacion */
	BILL_LIQUIDATION_DETAIL_MERGE

}
