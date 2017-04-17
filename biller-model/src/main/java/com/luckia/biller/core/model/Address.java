package com.luckia.biller.core.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import lombok.Data;

/**
 * Entidad que representa una direccion (provincia, municipio, codigo postal, direccion y
 * numero)
 */
@Entity
@Table(name = "B_ADDRESS")
@Data
@SuppressWarnings("serial")
public class Address implements Serializable, Mergeable<Address> {

	@Id
	@Column(name = "ID")
	@GeneratedValue
	private Long id;

	@Column(name = "ROAD")
	private String road;

	@Column(name = "NUMBER")
	private String number;

	@Column(name = "ZIP_CODE")
	private String zipCode;

	@ManyToOne
	@JoinColumn(name = "PROVINCE_ID")
	private Province province;

	@ManyToOne
	@JoinColumn(name = "REGION_ID")
	private Region region;

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.luckia.biller.core.jpa.Mergeable#merge(java.lang.Object)
	 */
	@Override
	public void merge(Address entity) {
		if (entity != null) {
			this.number = entity.number;
			this.province = entity.province;
			this.region = entity.region;
			this.road = entity.road;
			this.zipCode = entity.zipCode;
		}
	}
}