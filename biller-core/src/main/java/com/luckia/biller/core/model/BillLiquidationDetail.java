/*******************************************************************************
 * Copyright (c) 2004, 2012 Kotasoft S.L.
 * All rights reserved. This program and the accompanying materials
 * may only be used prior written consent of Kotasoft S.L.
 ******************************************************************************/
package com.luckia.biller.core.model;

import javax.persistence.Column;
import javax.persistence.Entity;

/**
 * Representa cada uno de los detalles que componen una liquidacion a nivel de factura.
 */
@Entity
@SuppressWarnings("serial")
public class BillLiquidationDetail extends AbstractBillDetail {

	/**
	 * Indica si el concepto forma parte de la liquidacion o queda fuera de esta.
	 */
	@Column(name = "LIQUIDATION_INCLUDED", nullable = false)
	private Boolean liquidationIncluded;

	public Boolean getLiquidationIncluded() {
		return liquidationIncluded;
	}

	public void setLiquidationIncluded(Boolean liquidationIncluded) {
		this.liquidationIncluded = liquidationIncluded;
	}
}
