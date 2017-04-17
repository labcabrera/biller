package com.luckia.biller.core.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.luckia.biller.core.serialization.NotSerializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entidad que representa uno de los estados posibles de una determinada entidad.
 * 
 * @see State
 * @see HasState
 */
@Entity
@Table(name = "S_STATE_DEFINITION")
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuppressWarnings("serial")
public class StateDefinition implements Serializable {

	@Id
	@Column(name = "ID", nullable = false, length = 32)
	private String id;

	@Column(name = "ENTITY_CLASS", nullable = false, columnDefinition = "VARCHAR(256)")
	@Convert(converter = ClassConverter.class)
	@NotSerializable
	private Class<? extends HasState> hasStateClass;

}
