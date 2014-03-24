/*******************************************************************************
 * Copyright (c) 2004, 2012 Kotasoft S.L.
 * All rights reserved. This program and the accompanying materials
 * may only be used prior written consent of Kotasoft S.L.
 ******************************************************************************/
package com.luckia.biller.core.model;

import java.math.BigDecimal;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.luckia.biller.core.jpa.Mergeable;

/**
 * Entidad que representa una factura. La aplicacion genera las facturas que emiten los establecimientos a sus operadores de máquinas de
 * juego.
 * <ul>
 * <li>En emisor de la factura será el titular del establecimiento.</li>
 * <li>El receptor de la factura será la empresa a la que está asociada el establecimiento</li>
 * <li>El código de la factura se generará secuencialmente a partir de la secuencia de cada uno de los clientes</li>
 * </ul>
 */
@Entity
@Table(name = "B_BILL")
@DiscriminatorValue("B")
@SuppressWarnings("serial")
@NamedQueries({ @NamedQuery(name = "Bill.selectPendingByReceiverInRange", query = "select b from Bill b where b.receiver = :receiver and b.billDate >= :from and b.billDate <= :to and b.liquidation is null") })
public class Bill extends AbstractBill implements Mergeable<Bill> {

	/** Lista de detalles que componen la factura */
	@OneToMany(cascade = CascadeType.DETACH, mappedBy = "bill")
	private List<BillDetail> details;

	@OneToMany(cascade = CascadeType.DETACH, mappedBy = "bill")
	private List<LiquidationDetail> liquidationDetails;

	@Column(name = "NET_AMOUNT", precision = 18, scale = 2)
	private BigDecimal netAmount;

	@Column(name = "VAT_AMOUNT", precision = 18, scale = 2)
	private BigDecimal vatAmount;

	@Column(name = "VAT_PERCENT", precision = 18, scale = 2)
	private BigDecimal vatPercent;

	@Column(name = "LIQUIDATION_AMOUNT", precision = 18, scale = 2)
	private BigDecimal liquidationAmount;

	@ManyToOne(cascade = CascadeType.DETACH)
	@JoinColumn(name = "LIQUIDATION_ID")
	protected Liquidation liquidation;

	public List<BillDetail> getDetails() {
		return details;
	}

	public void setDetails(List<BillDetail> details) {
		this.details = details;
	}

	public BigDecimal getNetAmount() {
		return netAmount;
	}

	public void setNetAmount(BigDecimal netAmount) {
		this.netAmount = netAmount;
	}

	public BigDecimal getVatAmount() {
		return vatAmount;
	}

	public void setVatAmount(BigDecimal vatAmount) {
		this.vatAmount = vatAmount;
	}

	public BigDecimal getVatPercent() {
		return vatPercent;
	}

	public void setVatPercent(BigDecimal vatPercent) {
		this.vatPercent = vatPercent;
	}

	public Liquidation getLiquidation() {
		return liquidation;
	}

	public void setLiquidation(Liquidation liquidation) {
		this.liquidation = liquidation;
	}

	public List<LiquidationDetail> getLiquidationDetails() {
		return liquidationDetails;
	}

	public void setLiquidationDetails(List<LiquidationDetail> liquidationDetails) {
		this.liquidationDetails = liquidationDetails;
	}

	public BigDecimal getLiquidationAmount() {
		return liquidationAmount;
	}

	public void setLiquidationAmount(BigDecimal liquidationAmount) {
		this.liquidationAmount = liquidationAmount;
	}

	@Override
	public String toString() {
		return String.format("Bill(code: %s, state: %s, amount: %s)", code, currentState, amount);
	}

	@Override
	public void merge(Bill entity) {
		this.billDate = entity.billDate;
		this.comments = entity.comments;
	}
}
