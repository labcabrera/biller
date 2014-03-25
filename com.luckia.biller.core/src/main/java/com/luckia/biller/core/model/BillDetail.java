/*******************************************************************************
 * Copyright (c) 2004, 2012 Kotasoft S.L.
 * All rights reserved. This program and the accompanying materials
 * may only be used prior written consent of Kotasoft S.L.
 ******************************************************************************/
package com.luckia.biller.core.model;

import javax.persistence.Entity;

/**
 * Representa cada uno de los detalles que componen una factura.
 */
@Entity
@SuppressWarnings("serial")
public class BillDetail extends AbstractBillDetail implements Cloneable {

	@Override
	public BillDetail clone() {
		try {
			return (BillDetail) super.clone();
		} catch (CloneNotSupportedException ex) {
			throw new RuntimeException(ex);
		}
	}
}
