package com.luckia.biller.core.model;

/**
 * Tipo enumerado que establece diferentes estados genéricos de las entidades factura y liquidacion.
 * 
 * @see AbstractBill
 * @see Bill
 * @see Liquidation
 * @see StateDefinition
 */
public enum CommonState {

	/**
	 * El elemento La factura ha sido creada y persistida pero aun no se han calculado los detalles que la componen.
	 */
	INITIAL,

	/**
	 * Elemento generado de forma automatica que está pendiente de ser validado por un operador de la aplicación.
	 */
	DRAFT,

	/**
	 * Elemento aprobado por un operador.
	 */
	CONFIRMED,

	/**
	 * Elemento enviado por correo.
	 */
	SENT,

	/**
	 * El elemento sin efecto carente de información.
	 */
	EMPTY,

	/**
	 * La factura ha sido rectificada.
	 */
	RECTIFIED;

}
