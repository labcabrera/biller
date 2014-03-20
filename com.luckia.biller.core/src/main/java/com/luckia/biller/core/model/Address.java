package com.luckia.biller.core.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.luckia.biller.core.jpa.Mergeable;

/**
 * Entidad que representa una direccion (provincia, municipio, codigo postal, direccion y numero)
 */
@Entity
@Table(name = "B_ADDRESS")
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

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getRoad() {
		return road;
	}

	public void setRoad(String road) {
		this.road = road;
	}

	public String getNumber() {
		return number;
	}

	public void setNumber(String number) {
		this.number = number;
	}

	public Province getProvince() {
		return province;
	}

	public void setProvince(Province province) {
		this.province = province;
	}

	public Region getRegion() {
		return region;
	}

	public void setRegion(Region region) {
		this.region = region;
	}

	public String getZipCode() {
		return zipCode;
	}

	public void setZipCode(String zipCode) {
		this.zipCode = zipCode;
	}

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