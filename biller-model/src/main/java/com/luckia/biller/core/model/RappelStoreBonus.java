package com.luckia.biller.core.model;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.luckia.biller.core.serialization.NotSerializable;

/**
 * Entidad que representa el bonus anual por rappel a una empresa.
 */
@Entity
@Table(name = "B_RAPPEL_STORE_BONUS")
public class RappelStoreBonus implements Auditable, HasState {

	@Id
	@Column(name = "ID", length = 36)
	protected String id;

	/**
	 * Liquidacion de bonus dentro de la cual esta contenida
	 */
	@NotSerializable
	@ManyToOne(cascade = CascadeType.DETACH)
	@JoinColumn(name = "RAPPEL_LIQUIDATION_ID", referencedColumnName = "ID", nullable = true)
	protected RappelLiquidation rappelLiquidation;

	@ManyToOne(cascade = CascadeType.DETACH, optional = false)
	@JoinColumn(name = "ID_STORE")
	protected Store store;

	@Column(name = "BONUS_DATE", nullable = false)
	@Temporal(TemporalType.DATE)
	protected Date bonusDate;

	@Column(name = "VALUE", precision = 18, scale = 2, nullable = false)
	protected BigDecimal value;

	@Column(name = "BASE_VALUE", precision = 18, scale = 2, nullable = false)
	protected BigDecimal baseValue;

	@Column(name = "PRORATA", precision = 18, scale = 2, nullable = true)
	protected BigDecimal prorata;

	@ManyToOne(cascade = CascadeType.PERSIST, fetch = FetchType.EAGER)
	@JoinColumn(name = "CURRENT_STATE")
	protected State currentState;

	/**
	 * Rango de rappel utilizado para generar el bonus
	 */
	@ManyToOne(cascade = CascadeType.DETACH, optional = true)
	@JoinColumn(name = "ID_RAPPEL")
	protected Rappel rappel;

	@Embedded
	protected AuditData auditData;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Store getStore() {
		return store;
	}

	public void setStore(Store store) {
		this.store = store;
	}

	public BigDecimal getValue() {
		return value;
	}

	public void setValue(BigDecimal value) {
		this.value = value;
	}

	public BigDecimal getBaseValue() {
		return baseValue;
	}

	public void setBaseValue(BigDecimal baseValue) {
		this.baseValue = baseValue;
	}

	public BigDecimal getProrata() {
		return prorata;
	}

	public void setProrata(BigDecimal prorata) {
		this.prorata = prorata;
	}

	public Date getBonusDate() {
		return bonusDate;
	}

	public void setBonusDate(Date bonusDate) {
		this.bonusDate = bonusDate;
	}

	@Override
	public State getCurrentState() {
		return currentState;
	}

	@Override
	public void setCurrentState(State value) {
		currentState = value;
	}

	@Override
	public AuditData getAuditData() {
		return auditData;
	}

	@Override
	public void setAuditData(AuditData value) {
		this.auditData = value;
	}

	public RappelLiquidation getRappelLiquidation() {
		return rappelLiquidation;
	}

	public void setRappelLiquidation(RappelLiquidation rappelLiquidation) {
		this.rappelLiquidation = rappelLiquidation;
	}

	public Rappel getRappel() {
		return rappel;
	}

	public void setRappel(Rappel rappel) {
		this.rappel = rappel;
	}
}
