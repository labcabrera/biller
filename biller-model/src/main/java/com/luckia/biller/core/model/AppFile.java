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

import lombok.Data;

/**
 * Entidad que representa un fichero dentro del repositorio de la aplicacion.
 */
@Entity
@Table(name = "S_APP_FILE")
@Data
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

}
