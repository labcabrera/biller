/*******************************************************************************
 * Copyright (c) 2004, 2012 Kotasoft S.L.
 * All rights reserved. This program and the accompanying materials
 * may only be used prior written consent of Kotasoft S.L.
 ******************************************************************************/
package com.luckia.biller.core.model;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.luckia.biller.core.serialization.NotSerializable;

/**
 * Representa una liquidación. A diferencia de las facturas las liquidaciones no devengan IVA. Las liquidaciones se realizan a las empresas
 * como un agregado de todas las facturas emitidas a los establecimientos pertenecientes a la empresa.<br>
 * Para cada empresa operadora se generan n facturas dependiendo de los centros de coste a los que estén asociados los establecimientos. Por
 * ejemplo, si una empresa opera en Galicia y Valencia, se generarán dos liquidaciones para esa empresa, una para cada comunidad autónoma.
 * <ul>
 * <li>El emisor de la factura será la empresa operadora</li>
 * <li>El receptor de la factura será el centro de coste</li>
 * </ul>
 * 
 */
@Entity
@Table(name = "B_LIQUIDATION")
@DiscriminatorValue("L")
@SuppressWarnings("serial")
public class Liquidation extends AbstractBill {

	/**
	 * Lista de facturas que componen la liquidación.
	 */
	@OneToMany(cascade = CascadeType.PERSIST)
	@JoinTable(name = "B_LIQUIDATION_BILL")
	@NotSerializable
	private List<Bill> bills;

	/**
	 * Lista de detalles (ajustes operativos) de la liquidación.
	 */
	private List<LiquidationDetail> details;

	public List<Bill> getBills() {
		return bills;
	}

	public void setBills(List<Bill> bills) {
		this.bills = bills;
	}

	public List<LiquidationDetail> getDetails() {
		return details;
	}

	public void setDetails(List<LiquidationDetail> details) {
		this.details = details;
	}

}
