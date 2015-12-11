package com.luckia.biller.core.model;

/**
 * Indica el tipo de actividad de usuario que guarda el sistema para auditar las acciones realizadas por los usuarios.
 */
public enum UserActivityType {

	BILL_CONFIRM,

	BILL_LIQUIDATION_DETAIL_MERGE,

	BILL_MERGE,

	BILL_RECALCULATION,

	BILL_REMOVE,

	BILLING_MODEL_MERGE,

	BILLING_MODEL_REMOVE,

	BILLING_MODEL_RAPPEL_REMOVE,

	BILLING_MODEL_RAPPEL_MERGE,

	COMPANY_MERGE,

	COMPANY_REMOVE,

	COMPANY_GROUP_REMOVE,

	COMPANY_GROUP_MERGE,

	COST_CENTER_MERGE,

	COST_CENTER_REMOVE,

	LIQUIDATION_APPROBATION,

	LIQUIDATION_MERGE_DETAIL,

	LIQUIDATION_RECALCULATION,

	LIQUIDATION_REMOVE,

	LIQUIDATION_REMOVE_DETAIL,

	TERMINAL_RELATION_MERGE,

	TERMINAL_RELATION_REMOVE,

	SEND_MAIL_LIQUIDATION,

	STORE_MERGE,

	STORE_REMOVE,

	OWNER_MERGE,

	OWNER_REMOVE,

	PROVINCE_TAX_MERGE,

	USER_MERGE

}
