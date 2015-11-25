package com.luckia.biller.core.model;

import java.io.Serializable;
import java.math.BigDecimal;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.luckia.biller.core.serialization.NotSerializable;

/**
 * Representa un ajuste realizado en la liquidacion.
 */
@Entity
@Table(name = "B_LIQUIDATION_DETAIL")
@SuppressWarnings("serial")
public class LiquidationDetail implements Mergeable<LiquidationDetail>, Serializable {

	@Id
	@Column(name = "ID", length = 36)
	private String id;

	@NotSerializable
	@ManyToOne(cascade = CascadeType.DETACH, optional = false)
	@JoinColumn(name = "LIQUIDATION_ID", referencedColumnName = "ID", nullable = false)
	private Liquidation liquidation;

	@Column(name = "NAME", length = 256)
	private String name;

	@Column(name = "UNITS", precision = 18, scale = 2)
	private BigDecimal units;

	@Column(name = "VALUE", precision = 18, scale = 2)
	private BigDecimal value;

	@Column(name = "SOURCE_VALUE", precision = 18, scale = 2)
	private BigDecimal sourceValue;

	@Column(name = "NET_VALUE", precision = 18, scale = 2)
	private BigDecimal netValue;

	@Column(name = "VAT_VALUE", precision = 18, scale = 2)
	private BigDecimal vatValue;

	/**
	 * Indica si el ajuste se aplica dentro del importe de liquidacion o se aplica fuera del importe a liquidar.
	 */
	@Column(name = "LIQUIDATION_INCLUDED", nullable = false)
	private Boolean liquidationIncluded;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Liquidation getLiquidation() {
		return liquidation;
	}

	public void setLiquidation(Liquidation liquidation) {
		this.liquidation = liquidation;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public BigDecimal getUnits() {
		return units;
	}

	public void setUnits(BigDecimal units) {
		this.units = units;
	}

	public BigDecimal getValue() {
		return value;
	}

	public void setValue(BigDecimal value) {
		this.value = value;
	}

	public Boolean getLiquidationIncluded() {
		return liquidationIncluded;
	}

	public void setLiquidationIncluded(Boolean liquidationIncluded) {
		this.liquidationIncluded = liquidationIncluded;
	}

	public BigDecimal getSourceValue() {
		return sourceValue;
	}

	public void setSourceValue(BigDecimal sourceValue) {
		this.sourceValue = sourceValue;
	}

	public BigDecimal getNetValue() {
		return netValue;
	}

	public void setNetValue(BigDecimal netValue) {
		this.netValue = netValue;
	}

	public BigDecimal getVatValue() {
		return vatValue;
	}

	public void setVatValue(BigDecimal vatValue) {
		this.vatValue = vatValue;
	}

	@Override
	public void merge(LiquidationDetail entity) {
		if (entity != null) {
			this.name = entity.name;
			this.units = entity.units;
			this.value = entity.value;
			this.liquidationIncluded = entity.liquidationIncluded;
			this.sourceValue = entity.sourceValue;
			this.netValue = entity.netValue;
			this.vatValue = entity.vatValue;
		}
	}
}
