package com.luckia.biller.core.model;

import java.io.Serializable;
import java.math.BigDecimal;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.luckia.biller.core.jpa.Mergeable;

/**
 * Entidad que representa las tasas de juego de una determinada provincia. En principio este valor será del 10% del GGR.
 */
@Entity
@Table(name = "B_BILLING_PROVINCE_FEES")
@SuppressWarnings("serial")
public class BillingProvinceFees implements Serializable, Mergeable<BillingProvinceFees> {

	@Id
	@GeneratedValue
	@Column(name = "ID")
	private Long id;

	@ManyToOne(cascade = CascadeType.DETACH, optional = false)
	@JoinColumn(name = "PROVINCE_ID")
	private Province province;

	/**
	 * Porcentaje de tasa de juego asociado a la provincia.
	 */
	@Column(name = "FEES_PERCENT", precision = 18, scale = 2, nullable = false)
	private BigDecimal feesPercent;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Province getProvince() {
		return province;
	}

	public void setProvince(Province province) {
		this.province = province;
	}

	public BigDecimal getFeesPercent() {
		return feesPercent;
	}

	public void setFeesPercent(BigDecimal feesPercent) {
		this.feesPercent = feesPercent;
	}

	@Override
	public void merge(BillingProvinceFees entity) {
		province = entity.province;
		feesPercent = entity.feesPercent;
	}
}
