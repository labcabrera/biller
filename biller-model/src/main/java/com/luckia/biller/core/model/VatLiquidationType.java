package com.luckia.biller.core.model;

/**
 * Indica como se aplica el calculo de IVA en la liquidacion.
 */
public enum VatLiquidationType {

	/**
	 * Sin IVA. Este es el equivalente al que se hizo inicialmente en el que la
	 * liquidacion al ser un negocio de explotacion conjunta carece de IVA.
	 */
	EXCLUDED,

	/**
	 * IVA incluido en el importe de la liquidacion (como lo hace las facturas). Por
	 * ejemplo, si el resultado de la liquidacion fuesen 1000€ habria que separarlos en
	 * dos conceptos, uno de 174 euros en concepto de iva y otro de 826.
	 */
	LIQUIDATION_INCLUDED,

	/**
	 * IVA incluido pero se añade sobre el importe de la liquidacion. Si por ejemplo el
	 * resultado de la liquidacion fueran 1000€ habria que añadir un concepto de IVA de
	 * 210€
	 */
	LIQUIDATION_ADDED

}
