package com.luckia.biller.core.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.luckia.biller.core.jpa.Mergeable;
import com.luckia.biller.core.serialization.NotSerializable;

@Entity
@Table(name = "B_BILL_DETAIL")
@SuppressWarnings("serial")
public abstract class AbstractBillDetail implements Serializable, Mergeable<AbstractBillDetail> {

	@Id
	@Column(name = "ID", length = 36)
	private String id;

	@NotSerializable
	@ManyToOne(cascade = CascadeType.DETACH, optional = false)
	@JoinColumn(name = "BILL_ID", referencedColumnName = "ID", nullable = false)
	private Bill bill;

	@Column(name = "CONCEPT_TYPE", length = 32)
	@Enumerated(EnumType.STRING)
	private BillConcept concept;

	@Column(name = "NAME", length = 256)
	private String name;

	@Column(name = "UNITS", precision = 18, scale = 2)
	private BigDecimal units;

	@Column(name = "PERCENT", precision = 18, scale = 2)
	private BigDecimal percent;

	@Column(name = "BASE_VALUE", precision = 18, scale = 2)
	private BigDecimal baseValue;

	@Column(name = "NET_VALUE", precision = 18, scale = 2)
	private BigDecimal netValue;

	@Column(name = "VAT_VALUE", precision = 18, scale = 2)
	private BigDecimal vatValue;

	@Column(name = "VAT_PERCENT", precision = 18, scale = 2)
	private BigDecimal vatPercent;

	@Column(name = "VALUE", precision = 18, scale = 2)
	private BigDecimal value;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public BillConcept getConcept() {
		return concept;
	}

	public void setConcept(BillConcept concept) {
		this.concept = concept;
	}

	public BigDecimal getValue() {
		return value;
	}

	public void setValue(BigDecimal value) {
		this.value = value;
	}

	public Bill getBill() {
		return bill;
	}

	public void setBill(Bill bill) {
		this.bill = bill;
	}

	public BigDecimal getUnits() {
		return units;
	}

	public void setUnits(BigDecimal units) {
		this.units = units;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public BigDecimal getBaseValue() {
		return baseValue;
	}

	public void setBaseValue(BigDecimal baseValue) {
		this.baseValue = baseValue;
	}

	public BigDecimal getPercent() {
		return percent;
	}

	public void setPercent(BigDecimal percent) {
		this.percent = percent;
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

	public BigDecimal getVatPercent() {
		return vatPercent;
	}

	public void setVatPercent(BigDecimal vatPercent) {
		this.vatPercent = vatPercent;
	}

	@Override
	public String toString() {
		return String.format("Detail (%s, %s) [Bill: %s] [Name: %s]", concept, value, bill, name);
	}

	@Override
	public void merge(AbstractBillDetail entity) {
		this.name = entity.name;
		this.units = entity.units != null ? entity.units.setScale(2, RoundingMode.HALF_EVEN) : null;
		this.value = entity.value != null ? entity.value.setScale(2, RoundingMode.HALF_EVEN) : null;
		this.concept = entity.concept;
	}
}
