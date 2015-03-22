package com.luckia.biller.core.model;

/**
 * Tipo enumerado que establece diferentes estados genéricos de las entidades.
 */
public enum CommonState {
	/**
	 * El elemento La factura ha sido creada y persistida pero aun no se han calculado los detalles que la componen.
	 */
	Initial("Creada"),

	/**
	 * Elemento generado de forma automatica que está pendiente de ser validado por un operador de la aplicación.
	 */
	Draft("Borrador"),

	/**
	 * Elemento aprobado por un operador.
	 */
	Confirmed("Confirmada"),

	/** Elemento enviado por correo. */
	Sent("Enviada"),

	/** El elemento sin efecto carente de información.  */
	Empty("Sin resultado"),

	/** La factura se ha rectificado. */
	Rectified("Rectificada");

	private String desc;

	private CommonState(String desc) {
		this.desc = desc;
	}

	/**
	 * Obtiene el literal asociado al estado.
	 * 
	 * @return literal
	 */
	public String desc() {
		return desc;
	}
}
