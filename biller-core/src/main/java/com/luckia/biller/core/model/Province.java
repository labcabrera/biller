/*******************************************************************************
 * Copyright (c) 2004, 2012 Kotasoft S.L.
 * All rights reserved. This program and the accompanying materials
 * may only be used prior written consent of Kotasoft S.L.
 ******************************************************************************/
package com.luckia.biller.core.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.eclipse.persistence.annotations.Index;

/**
 * Representa una provincia
 */
@Entity
@Table(name = "S_PROVINCE")
@SuppressWarnings("serial")
public class Province implements Serializable {

	@Id
	@Column(name = "ID")
	private String id;

	@Column(name = "CODE", length = 2, nullable = false)
	@Index(name = "IX_PROVINCE_CODE")
	private String code;

	@Column(name = "NAME", length = 36, nullable = false)
	private String name;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return name;
	}
}
