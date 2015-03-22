package com.luckia.biller.core.model.lis;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * Representa un registro de facturacion de la tabla de LIS <b>terminales_por_mes</b>.
 * 
 */
@Entity
@Table(name = "terminales_por_mes")
@IdClass(LisTerminalRecordPK.class)
@NamedQueries({ @NamedQuery(name = "LisTerminalRecord.selectByCodesInRange", query = "select e from LisTerminalRecord e where e.terminalCode in :codes and e.date >= :from and e.date <= :to") })
public class LisTerminalRecord {

	@Id
	@Column(name = "fecha")
	@Temporal(TemporalType.DATE)
	private Date date;

	@Id
	@Column(name = "terminal")
	private String terminalCode;

	@Column(name = "apostado")
	private BigDecimal betAmount;

	@Column(name = "premios")
	private BigDecimal winAmount;

	@Column(name = "cancelado")
	private BigDecimal cancelledAmount;

	@Column(name = "imputables")
	private BigDecimal attributable;

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public String getTerminalCode() {
		return terminalCode;
	}

	public void setTerminalCode(String terminalCode) {
		this.terminalCode = terminalCode;
	}

	public BigDecimal getBetAmount() {
		return betAmount;
	}

	public void setBetAmount(BigDecimal betAmount) {
		this.betAmount = betAmount;
	}

	public BigDecimal getWinAmount() {
		return winAmount;
	}

	public void setWinAmount(BigDecimal winAmount) {
		this.winAmount = winAmount;
	}

	public BigDecimal getCancelledAmount() {
		return cancelledAmount;
	}

	public void setCancelledAmount(BigDecimal cancelledAmount) {
		this.cancelledAmount = cancelledAmount;
	}

	public BigDecimal getAttributable() {
		return attributable;
	}

	public void setAttributable(BigDecimal attributable) {
		this.attributable = attributable;
	}
}
