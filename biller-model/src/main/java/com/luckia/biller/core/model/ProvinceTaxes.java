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
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import lombok.Data;

/**
 * Entidad que representa las tasas de juego de una determinada provincia. En principio
 * este valor será del 10% del GGR.
 */
@Entity
@Table(name = "B_PROVINCE_TAXES")
@NamedQueries({
		@NamedQuery(name = "ProvinceTaxes.selectAll", query = "select e from ProvinceTaxes e order by e.province.name"),
		@NamedQuery(name = "ProvinceTaxes.selectByProvince", query = "select e from ProvinceTaxes e where e.province = :province") })
@Data
@SuppressWarnings("serial")
public class ProvinceTaxes implements Serializable, Mergeable<ProvinceTaxes> {

	public static final String QUERY_SELECT_BY_PROVINCE = "ProvinceTaxes.selectByProvince";

	@Id
	@GeneratedValue
	@Column(name = "ID")
	private Long id;

	@ManyToOne(cascade = CascadeType.DETACH, optional = false)
	@JoinColumn(name = "PROVINCE_ID", unique = true)
	private Province province;

	/**
	 * Porcentaje de tasa de juego asociado a la provincia.
	 */
	@Column(name = "FEES_PERCENT", precision = 18, scale = 2, nullable = false)
	private BigDecimal feesPercent;

	/**
	 * IVA aplicado en la provincia.
	 */
	@Column(name = "VAT_PERCENT", precision = 18, scale = 2, nullable = false)
	private BigDecimal vatPercent;

	@Override
	public void merge(ProvinceTaxes entity) {
		province = entity.province;
		feesPercent = entity.feesPercent;
	}
}
