package com.luckia.biller.core.model;

import java.io.Serializable;
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

import lombok.Data;

/**
 * Entidad que representa el bonus anual por rappel a una empresa.
 */
@Entity
@Table(name = "B_RAPPEL_STORE_BONUS")
@Data
@SuppressWarnings("serial")
public class RappelStoreBonus implements Auditable, HasState, Serializable {

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

}
