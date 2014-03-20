/*******************************************************************************
 * Copyright (c) 2004, 2012 Kotasoft S.L.
 * All rights reserved. This program and the accompanying materials
 * may only be used prior written consent of Kotasoft S.L.
 ******************************************************************************/
package com.luckia.biller.core.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name = "S_ACTION_EXECUTION")
@SuppressWarnings("serial")
public class ActionExecution implements Serializable {

	@Id
	@Column(name = "ID")
	private String id;

	@Column(name = "ENTITY_CLASS", nullable = false)
	private String entityClass;

	@Lob
	@Basic(fetch = FetchType.LAZY)
	@Column(name = "ACTION_DATA", columnDefinition = "BLOB")
	private String actionData;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "EXECUTION")
	private Date execution;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "EXECUTED")
	private Date executed;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "CREATED", nullable = false)
	private Date created;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "CANCELLED")
	private Date cancelled;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getEntityClass() {
		return entityClass;
	}

	public void setEntityClass(String entityClass) {
		this.entityClass = entityClass;
	}

	public String getActionData() {
		return actionData;
	}

	public void setActionData(String actionData) {
		this.actionData = actionData;
	}

	public Date getExecution() {
		return execution;
	}

	public void setExecution(Date execution) {
		this.execution = execution;
	}

	public Date getExecuted() {
		return executed;
	}

	public void setExecuted(Date executed) {
		this.executed = executed;
	}

	public Date getCreated() {
		return created;
	}

	public void setCreated(Date created) {
		this.created = created;
	}

	public Date getCancelled() {
		return cancelled;
	}

	public void setCancelled(Date cancelled) {
		this.cancelled = cancelled;
	}
}
