package com.luckia.biller.core.model;

/**
 * Tipo enumerado que establece los diferentes valores de los estados de una factura.
 */
public enum BillState {
	/**
	 * La factura ha sido creada y persistida pero aun no se han calculado los detalles que la componen
	 */
	BillInitial("Creada"),

	/**
	 * Se ha calculado de forma automatica el resultado de la factura pero aun no ha sido aceptada por el operador de la aplicacion
	 */
	BillDraft("Borrador"),

	/**
	 * La factura ha sido aceptada por el operador pero aun no se ha enviado al destinatario
	 */
	BillConfirmed("Confirmada"),

	/** La factura se ha enviado al destinatario */
	BillSent("Enviada"),

	/** La factura se ha cancelado */
	BillCancelled("Cancelada"),

	/** No hay ningun detalle que facturar */
	BillEmpty("Sin resultado");

	private String desc;

	private BillState(String desc) {
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
