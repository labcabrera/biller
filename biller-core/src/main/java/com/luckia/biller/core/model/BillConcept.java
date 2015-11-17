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
	STAKES,

	/** Bonus fijo */
	@Deprecated
	BONUS,

	/** Gastos mensuales de atención comercial */
	COMMERCIAL_MONTHLY_FEES,

	/** Gastos mensuales de co-explotacion */
	COOPERATING_MONTHLY_FEES,

	/** Gastos mensuales de servicio de atencion al cliente */
	SAT_MONTHLY_FEES,

	/** Coste por ubicacion */
	PRICE_PER_LOCATION,

	/** Saldo de caja */
	STORE_CASH,

	/** Credito */
	CREDIT,

	/** Ajuste manual */
	MANUAL,

	/** Ajuste operativo */
	@Deprecated
	ADJUSTMENT,

	/**
	 * Ajuste manual incluído en la liquidación
	 */
	@Deprecated
	MANUAL_WITH_LIQUIDATION,

	/**
	 * Ajuste manual no incluído en la liquidación
	 */
	@Deprecated
	MANUAL_WITHOUT_LIQUIDATION,

	/** Cualquier otro concepto de facturacion que no este incluido en el modelo */
	@Deprecated
	OTHER,

	/** Total apostado. */
	TOTAL_BET_AMOUNT,

	/** Cancelaciones. */
	CANCELLED,

	/** Total premiado */
	TOTAL_WIN_AMOUNT,

	/** Total imputable */
	TOTAL_ATTRIBUTABLE,

	/** Margen */
	MARGIN;
}
