/*******************************************************************************
 * Copyright (c) 2004, 2012 Kotasoft S.L.
 * All rights reserved. This program and the accompanying materials
 * may only be used prior written consent of Kotasoft S.L.
 ******************************************************************************/
package com.luckia.biller.core.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.luckia.biller.core.serialization.NotSerializable;

/**
 * Entidad que representa un fichero dentro del repositorio de la aplicacion.
 */
@Entity
@Table(name = "S_APP_FILE")
@SuppressWarnings("serial")
public class AppFile implements Serializable {

	@Id
	@GeneratedValue
	private Long id;

	@Column(name = "CONTENT_TYPE", nullable = false)
	private String contentType;

	@Column(name = "NAME", nullable = false)
	private String name;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "GENERATED", nullable = false)
	private Date generated;

	@Column(name = "PATH")
	@NotSerializable
	private String internalPath;

	@Column(name = "FILE_SIZE")
	private Long size;

	public Date getGenerated() {
		return generated;
	}

	public void setGenerated(Date generated) {
		this.generated = generated;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getInternalPath() {
		return internalPath;
	}

	public void setInternalPath(String internalPath) {
		this.internalPath = internalPath;
	}

	public Long getSize() {
		return size;
	}

	public void setSize(Long size) {
		this.size = size;
	}
}
