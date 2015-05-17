/*******************************************************************************
 * Copyright (c) 2004, 2012 Kotasoft S.L.
 * All rights reserved. This program and the accompanying materials
 * may only be used prior written consent of Kotasoft S.L.
 ******************************************************************************/
package com.luckia.biller.core.model;

/**
 * Listado de todos los tipos conocidos de conceptos de facturacion y liquidacion aplicados.
 */
public enum BillConcept {

	/** Resultado neto (NGR - Gastos operativos) */
	NR,

	/**
	 * Ingresos netos del juego: Net Gaming Revenue = GGR – Tasa juego<br>
	 * La Tasa de juego es una tasa autonómica que actualmente es un 10% del GGR en todas las Comunidades Autónomas.
	 * */
	NGR,

	/**
	 * Ingresos brutos de juego: Gross Gaming Revenue = Stakes – Winnings
	 */
	GGR,

	/**
	 * Importe apostado. Importe de los tickets de aa.dd. vendidos en un local para el periodo determinado.
	 */
	Stakes,

	/** Bonus fijo */
	Bonus,

	/** Gastos mensuales de atención comercial */
	CommercialMonthlyFees,

	/** Gastos mensuales de co-explotacion */
	CoOperatingMonthlyFees,

	/** Gastos mensuales de servicio de atencion al cliente */
	SatMonthlyFees,

	/** Coste por ubicacion */
	PricePerLocation,

	/** Saldo de caja */
	StoreCash,

	/** Ajuste operativo */
	Adjustment,

	/**
	 * Ajuste manual incluído en la liquidación
	 */
	ManualWithLiquidation,

	/**
	 * Ajuste manual no incluído en la liquidación
	 */
	ManualWithoutLiquidation,

	/** Cualquier otro concepto de facturacion que no este incluido en el modelo */
	Other,

	/** Total apostado. */
	TotalBetAmount,

	/** Cancelaciones. */
	Cance1lled, TotalWinAmount, TotalAttributable, Margin;
}
