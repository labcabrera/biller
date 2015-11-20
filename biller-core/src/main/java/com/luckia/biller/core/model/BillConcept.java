package com.luckia.biller.core.model;

/**
 * Listado de todos los tipos conocidos de conceptos de facturacion y liquidacion aplicados.
 */
public enum BillConcept {

	/** Resultado neto (NGR - Gastos operativos) */
	NR("NR"),

	/**
	 * Ingresos netos del juego: Net Gaming Revenue = GGR – Tasa juego<br>
	 * La Tasa de juego es una tasa autonómica que actualmente es un 10% del GGR en todas las Comunidades Autónomas.
	 */
	NGR("NGR"),

	/**
	 * Ingresos brutos de juego: Gross Gaming Revenue = Stakes – Winnings
	 */
	GGR("GGR"),

	/**
	 * Importe apostado. Importe de los tickets de aa.dd. vendidos en un local para el periodo determinado.
	 */
	STAKES("Ventas"),

	/** Gastos mensuales de atención comercial */
	COMMERCIAL_MONTHLY_FEES("Atención comercial"),

	/** Gastos mensuales de co-explotacion */
	COOPERATING_MONTHLY_FEES("Co-explotación"),

	/** Gastos mensuales de servicio de atencion al cliente */
	SAT_MONTHLY_FEES("SAT"),

	/** Coste por ubicacion */
	PRICE_PER_LOCATION("Coste por ubicación"),

	/** Saldo de caja */
	STORE_CASH("Saldo de caja"),

	CREDIT("Crédito"),

	MANUAL("Ajuste manual"),

	TOTAL_BET_AMOUNT("Total apostado"),

	CANCELLED("Cancelado"),

	TOTAL_WIN_AMOUNT("Premiado"),

	TOTAL_ATTRIBUTABLE("Imputable"),

	MARGIN("Margen"),

	BETTING_FEES("Honorarios por apuestas"),

	RAPPEL("Rappel"),

	LOAN_RECOVERY("Recuperación de préstamo"),

	ROBBERY("Robo"),

	@Deprecated BONUS("Bonus"),

	@Deprecated ADJUSTMENT("Ajuste manual"),

	/**
	 * Ajuste manual incluído en la liquidación
	 */
	@Deprecated MANUAL_WITH_LIQUIDATION("Ajuste manual incluído en la liquidación"),

	/**
	 * Ajuste manual no incluído en la liquidación
	 */
	@Deprecated MANUAL_WITHOUT_LIQUIDATION("Ajuste manual no incluído en la liquidación"),

	/** Cualquier otro concepto de facturacion que no este incluido en el modelo */
	@Deprecated OTHER("Otros");

	private String description;

	private BillConcept(String description) {
		this.description = description;
	}

	public String description() {
		return description;
	}

}
