package com.luckia.biller.core.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.eclipse.persistence.annotations.Convert;
import org.eclipse.persistence.annotations.Converter;

import com.luckia.biller.core.serialization.NotSerializable;

/**
 * Entidad que representa uno de los estados posibles de una determinada entidad.
 * 
 * @see State
 * @see HasState
 */
@Entity
@Table(name = "S_STATE_DEFINITION")
@SuppressWarnings("serial")
@Converter(converterClass = ClassConverter.class, name = "ClassConverter")
public class StateDefinition implements Serializable {

	@Id
	@Column(name = "ID", nullable = false, length = 32)
	private String id;

	@Column(name = "ENTITY_CLASS", nullable = false, columnDefinition = "VARCHAR(256)")
	@Convert("ClassConverter")
	@NotSerializable
	private Class<? extends HasState> hasStateClass;

	@Column(name = "STATE_DESC", nullable = false, length = 64)
	private String desc;

	public StateDefinition() {
	}

	public StateDefinition(String id, Class<? extends HasState> hasStateClass, String desc) {
		this.id = id;
		this.hasStateClass = hasStateClass;
		this.desc = desc;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Class<? extends HasState> getHasStateClass() {
		return hasStateClass;
	}

	public void setHasStateClass(Class<? extends HasState> hasStateClass) {
		this.hasStateClass = hasStateClass;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}
}
