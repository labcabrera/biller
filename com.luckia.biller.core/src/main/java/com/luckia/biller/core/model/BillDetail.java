/*******************************************************************************
 * Copyright (c) 2004, 2012 Kotasoft S.L.
 * All rights reserved. This program and the accompanying materials
 * may only be used prior written consent of Kotasoft S.L.
 ******************************************************************************/
package com.luckia.biller.core.model;

import javax.persistence.Column;
import javax.persistence.Entity;

/**
 * Representa cada uno de los detalles que componen una factura.
 */
@Entity
@SuppressWarnings("serial")
public class BillDetail extends AbstractBillDetail implements Cloneable {

	/**
	 * En caso de que este valor esté a <true>code</true> también se incluye en el resultado de la liquidación además de la factura.
	 */
	@Column(name = "PROPAGATE")
	private Boolean propagate;

	public Boolean getPropagate() {
		return propagate;
	}

	public void setPropagate(Boolean propagate) {
		this.propagate = propagate;
	}

	@Override
	public BillDetail clone() {
		try {
			return (BillDetail) super.clone();
		} catch (CloneNotSupportedException ex) {
			throw new RuntimeException(ex);
		}
	}

	@Override
	public void merge(AbstractBillDetail entity) {
		if (entity != null) {
			super.merge(entity);
			if (BillDetail.class.isAssignableFrom(entity.getClass())) {
				propagate = ((BillDetail) entity).propagate;
			}
		}
	}
}
