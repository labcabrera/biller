package com.luckia.biller.core.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Entidad que representa una secuencia incremental asociada a un determinado identificador. Un ejemplo de esto sería la generacion de
 * números de facturas.
 * 
 * @see com.luckia.biller.core.jpa.Sequencer
 */
@Entity
@Table(name = "S_SEQUENCE")
public class Sequence {

	@Id
	@Column(name = "ID", length = 36)
	private String id;

	@Column(name = "VALUE")
	private Long value;

	public Sequence() {
	}

	public Sequence(String id, Long value) {
		this.id = id;
		this.value = value;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Long getValue() {
		return value;
	}

	public void setValue(Long value) {
		this.value = value;
	}
}
