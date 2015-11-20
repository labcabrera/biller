package com.luckia.biller.core.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * Entidad que representa un determinado estado para una entidad. El estado está asociado a uno de los {@link StateDefinition} registrados
 * para la entidad.
 * 
 * @see HasState
 * @see StateDefinition
 */
@Entity
@Table(name = "S_STATE")
@SuppressWarnings("serial")
public class State implements Serializable {

	@Id
	@Column(name = "ID", length = 36)
	private String id;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "ENTERED")
	private Date entered;

	@ManyToOne(cascade = CascadeType.DETACH)
	@JoinColumn(name = "STATE_DEFINITION_ID")
	private StateDefinition stateDefinition;

	public State() {
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Date getEntered() {
		return entered;
	}

	public void setEntered(Date entered) {
		this.entered = entered;
	}

	public StateDefinition getStateDefinition() {
		return stateDefinition;
	}

	public void setStateDefinition(StateDefinition stateDefinition) {
		this.stateDefinition = stateDefinition;
	}

	@Override
	public String toString() {
		return stateDefinition != null ? stateDefinition.getId() : super.toString();
	}
}
