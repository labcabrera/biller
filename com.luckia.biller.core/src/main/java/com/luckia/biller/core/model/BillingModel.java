/*******************************************************************************
 * Copyright (c) 2004, 2012 Kotasoft S.L.
 * All rights reserved. This program and the accompanying materials
 * may only be used prior written consent of Kotasoft S.L.
 ******************************************************************************/
package com.luckia.biller.core.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.luckia.biller.core.jpa.Mergeable;

/**
 * Entidad que establece la parametrización con la que se van a generar tanto facturas como liquidaciones.
 */
@Entity
@Table(name = "B_BILLING_MODEL")
@NamedQueries({ @NamedQuery(name = "BillingModel.selectAll", query = "select e from BillingModel e order by e.name") })
@SuppressWarnings("serial")
public class BillingModel implements Serializable, Mergeable<BillingModel>, Auditable {

	@Id
	@GeneratedValue
	@Column(name = "ID")
	private Long id;

	@Column(name = "NAME", length = 256, nullable = false)
	private String name;

	@Column(name = "MODEL_TYPE", length = 32, nullable = false)
	@Enumerated(EnumType.STRING)
	private BillingModelType type;

	/**
	 * Resultado neto
	 */
	@Column(name = "NR_PERCENT", precision = 18, scale = 2)
	private BigDecimal nrPercent;

	/**
	 * Ingresos netos de juego
	 */
	@Column(name = "NGR_PERCENT", precision = 18, scale = 2)
	private BigDecimal ngrPercent;

	/**
	 * Ingresos netos de juego
	 */
	@Column(name = "GGR_PERCENT", precision = 18, scale = 2)
	private BigDecimal ggrPercent;

	/**
	 * Importe apostado
	 */
	@Column(name = "STAKES_PERCENT", precision = 18, scale = 2)
	private BigDecimal stakesPercentStore;

	@Column(name = "STAKES_PERCENT_OPERATOR", precision = 18, scale = 2)
	private BigDecimal stakesPercentOperator;

	/**
	 * Gastos mensuales de co-explotacionl. Este valor se utiliza como un importe fijo a la hora de calcular el NR (recordar que el NR es el
	 * resultado de restar al NGR los gastos operativos).<br>
	 * A diferencia de los gastos comerciales y gastos del servicio de atencion al cliente este valor no se añade como conceptor en las
	 * facturas/liquidaciones.
	 */
	@Column(name = "CO_OP_MONTLY_FEES", precision = 18, scale = 2)
	private BigDecimal coOperatingMonthlyFees;

	/**
	 * Gastos mensuales de atencion comercial
	 */
	@Column(name = "COMMERCIAL_MONTLY_FEES", precision = 18, scale = 2)
	private BigDecimal commercialMonthlyFees;

	/**
	 * Gastos mensuales SAT
	 */
	@Column(name = "SAT_MONTLY_FEES", precision = 18, scale = 2)
	private BigDecimal satMonthlyFees;

	@OneToMany(cascade = CascadeType.PERSIST, mappedBy = "model")
	@OrderBy("amount")
	private List<Rappel> rappel;

	@Column(name = "CREATED")
	@Temporal(TemporalType.TIMESTAMP)
	private Date created;

	@Column(name = "DELETED")
	@Temporal(TemporalType.TIMESTAMP)
	private Date deleted;

	@Column(name = "MODIFIED")
	@Temporal(TemporalType.TIMESTAMP)
	private Date modified;

	@ManyToOne(cascade = CascadeType.DETACH)
	@JoinColumn(name = "MODIFIED_BY")
	private User modifiedBy;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public BillingModelType getType() {
		return type;
	}

	public void setType(BillingModelType type) {
		this.type = type;
	}

	public BigDecimal getNrPercent() {
		return nrPercent;
	}

	public void setNrPercent(BigDecimal nrPercent) {
		this.nrPercent = nrPercent;
	}

	public BigDecimal getNgrPercent() {
		return ngrPercent;
	}

	public void setNgrPercent(BigDecimal ngrPercent) {
		this.ngrPercent = ngrPercent;
	}

	public BigDecimal getGgrPercent() {
		return ggrPercent;
	}

	public void setGgrPercent(BigDecimal ggrPercent) {
		this.ggrPercent = ggrPercent;
	}

	public BigDecimal getStakesPercentStore() {
		return stakesPercentStore;
	}

	public void setStakesPercentStore(BigDecimal value) {
		this.stakesPercentStore = value;
	}

	public BigDecimal getStakesPercentOperator() {
		return stakesPercentOperator;
	}

	public void setStakesPercentOperator(BigDecimal stakesPercentOperator) {
		this.stakesPercentOperator = stakesPercentOperator;
	}

	public BigDecimal getCoOperatingMonthlyFees() {
		return coOperatingMonthlyFees;
	}

	public void setCoOperatingMonthlyFees(BigDecimal coOperatingMonthlyFees) {
		this.coOperatingMonthlyFees = coOperatingMonthlyFees;
	}

	public BigDecimal getCommercialMonthlyFees() {
		return commercialMonthlyFees;
	}

	public void setCommercialMonthlyFees(BigDecimal commercialMonthlyFees) {
		this.commercialMonthlyFees = commercialMonthlyFees;
	}

	public BigDecimal getSatMonthlyFees() {
		return satMonthlyFees;
	}

	public void setSatMonthlyFees(BigDecimal satMonthlyFees) {
		this.satMonthlyFees = satMonthlyFees;
	}

	public List<Rappel> getRappel() {
		return rappel;
	}

	public void setRappel(List<Rappel> rappel) {
		this.rappel = rappel;
	}

	public Date getCreated() {
		return created;
	}

	public void setCreated(Date created) {
		this.created = created;
	}

	public Date getDeleted() {
		return deleted;
	}

	public void setDeleted(Date deleted) {
		this.deleted = deleted;
	}

	public Date getModified() {
		return modified;
	}

	public void setModified(Date modified) {
		this.modified = modified;
	}

	public User getModifiedBy() {
		return modifiedBy;
	}

	public void setModifiedBy(User modifiedBy) {
		this.modifiedBy = modifiedBy;
	}

	@Override
	public void merge(BillingModel entity) {
		if (entity != null) {
			this.commercialMonthlyFees = entity.commercialMonthlyFees;
			this.coOperatingMonthlyFees = entity.coOperatingMonthlyFees;
			this.ggrPercent = entity.ggrPercent;
			this.name = entity.name;
			this.ngrPercent = entity.ngrPercent;
			this.nrPercent = entity.nrPercent;
			this.satMonthlyFees = entity.satMonthlyFees;
			this.stakesPercentStore = entity.stakesPercentStore;
			this.stakesPercentOperator = entity.stakesPercentOperator;
		}
	}
}
