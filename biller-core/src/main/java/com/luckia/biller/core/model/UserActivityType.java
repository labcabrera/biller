package com.luckia.biller.core.model;

/**
 * Indica el tipo de actividad de usuario que guarda el sistema para auditar las acciones realizadas por los usuarios.
 */
public enum UserActivityType {

	BILL_CONFIRM,

	BILL_LIQUIDATION_DETAIL_MERGE,

	BILL_MERGE,

	BILL_REMOVE,

	BILLING_MODEL_MERGE,

	BILLING_MODEL_REMOVE,

	BILLING_MODEL_RAPEL_REMOVE,

	BILLING_MODEL_RAPEL_MERGE,

	COST_CENTER_DELETE,

	COST_CENTER_MERGE,

	LIQUIDATION_APPROBATION,

	LIQUIDATION_CALCULATION,

	LIQUIDATION_MERGE_DETAIL,

	LIQUIDATION_REMOVE,

	LIQUIDATION_REMOVE_DETAIL,

}
