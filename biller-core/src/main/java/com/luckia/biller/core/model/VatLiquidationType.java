package com.luckia.biller.core.model;

/**
 * Indica como se aplica el calculo de IVA en la liquidacion.
 */
public enum VatLiquidationType {

	/** Sin IVA */
	EXCLUDED,

	/** IVA incluido en el importe de la liquidacion (como lo hace las facturas) */
	LIQUIDATION_BILL,

	/** IVA incluido pero se añade sobre el importe de la liquidacion */
	LIQUIDATION

}
