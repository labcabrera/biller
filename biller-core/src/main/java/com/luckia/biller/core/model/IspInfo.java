/*******************************************************************************
 * Copyright (c) 2004, 2012 Kotasoft S.L.
 * All rights reserved. This program and the accompanying materials
 * may only be used prior written consent of Kotasoft S.L.
 ******************************************************************************/
package com.luckia.biller.core.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Entidad que representa la información de conexión a internet de un establecimiento.
 */
@Entity
@Table(name = "B_ISO_INFO")
public class IspInfo {

	@Id
	@GeneratedValue
	private Long id;

	@Column(name = "CONNECTION_TYPE", length = 64)
	public String connectionType;

	@Column(name = "TRANSFER_RATE", length = 64)
	public String transferRate;

	@Column(name = "STATUS", length = 64)
	public String status;
	
	@Column(name = "PHONE_NUMBER", length = 64)
	public String phoneNumber;
	
	@Column(name = "ISSUE_PHONE_NUMBER", length = 64)
	public String issuePhoneNumber;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getConnectionType() {
		return connectionType;
	}

	public void setConnectionType(String connectionType) {
		this.connectionType = connectionType;
	}

	public String getTransferRate() {
		return transferRate;
	}

	public void setTransferRate(String transferRate) {
		this.transferRate = transferRate;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
}
