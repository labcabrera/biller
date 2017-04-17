package com.luckia.biller.core.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.luckia.biller.core.serialization.NotSerializable;

import lombok.Data;

/**
 * Representa un municipio dentro de una provincia.
 */
@Entity
@Table(name = "S_REGION")
@Data
@SuppressWarnings("serial")
public class Region implements Serializable {

	@Id
	@Column(name = "ID")
	private String id;

	@Column(name = "CODE", nullable = false)
	private String code;

	@Column(name = "NAME", nullable = false)
	private String name;

	@ManyToOne
	@JoinColumn(name = "PROVINCE_ID")
	@NotSerializable
	private Province province;

	@Override
	public String toString() {
		return name;
	}
}
