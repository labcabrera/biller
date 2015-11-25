package com.luckia.biller.core.model;

/**
 * Esta interfaz engloba todas las entidades del modelo en las que queremos tener una mínima trazabilidad sobre las modificaciones que se
 * realizan sobre ella. Generalmente se aplicará a todas las entidades que pueden ser modificadas desde la aplicación por un usuario. La
 * trazabilidad nos proporciona los siguientes valores:
 * <ul>
 * <li>Fecha de creación de la entidad</li>
 * <li>Fecha de modificación de la entidad</li>
 * <li>Fecha de borrado lógico de la entidad</li>
 * <li>Usuario que realiza la última modificación sobre la entidad</li>
 * </ul>
 * 
 * @see AuditData
 */
public interface Auditable {

	/**
	 * Obtiene la información de auditoria asociada la entidad.
	 * 
	 * @return
	 */
	AuditData getAuditData();

	/**
	 * Asocia la información de auditoría asociada a la entidad.
	 * 
	 * @param value
	 */
	void setAuditData(AuditData value);
}
