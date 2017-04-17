package com.luckia.biller.core.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import lombok.Data;

/**
 * Entidad que agrupa los diferentes atributos que queremos almacenar para tener una
 * mínima trazabilidad sobre las entidades:
 * <ul>
 * <li>Fecha de creación</li>
 * <li>Fecha de modificación</li>
 * <li>Fecha de eliminación</li>
 * <li>Usuario que realiza la última modificación</li>
 * </ul>
 * 
 * @see com.luckia.biller.core.services.AuditService
 * 
 */
@Embeddable
@Data
@SuppressWarnings("serial")
public class AuditData implements Serializable {

	@Column(name = "CREATED", nullable = false, updatable = false)
	@Temporal(TemporalType.TIMESTAMP)
	private Date created;

	@Column(name = "DELETED")
	@Temporal(TemporalType.TIMESTAMP)
	private Date deleted;

	@Column(name = "MODIFIED")
	@Temporal(TemporalType.TIMESTAMP)
	private Date modified;

	@ManyToOne(cascade = CascadeType.DETACH)
	@JoinColumn(name = "MODIFIED_BY")
	private User modifiedBy;

}
