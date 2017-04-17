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

import lombok.Data;

/**
 * Representa un ajuste realizado en la liquidacion.
 */
@Entity
@Table(name = "B_LIQUIDATION_DETAIL")
@Data
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
	 * Indica si el ajuste se aplica dentro del importe de liquidacion o se aplica fuera
	 * del importe a liquidar.
	 */
	@Column(name = "LIQUIDATION_INCLUDED", nullable = false)
	private Boolean liquidationIncluded;

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
