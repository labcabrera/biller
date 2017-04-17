package com.luckia.biller.core.services.bills;

/**
 * Servicio encargado de generar los códigos de una deteriminada entidad. Por ejemplo los
 * números de factura o de liquidaciones.
 * 
 * @param <T> Entidad asociada al servicio
 */
public interface CodeGenerator<T> {

	/**
	 * Actualiza la entidad estableciendo el un nuevo codigo (sin persistirla).
	 * 
	 * @param entity
	 */
	void generateCode(T entity);

}
