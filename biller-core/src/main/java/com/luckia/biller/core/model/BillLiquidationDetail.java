package com.luckia.biller.core.model;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 * Representa cada uno de los detalles que componen una liquidacion a nivel de factura.
 */
@Entity
@SuppressWarnings("serial")
@DiscriminatorValue("L")
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

	@Override
	public void merge(AbstractBillDetail entity) {
		super.merge(entity);
		if (entity != null && BillLiquidationDetail.class.isAssignableFrom(entity.getClass())) {
			BillLiquidationDetail e = (BillLiquidationDetail) entity;
			this.liquidationIncluded = e.liquidationIncluded;
		}
	}
}
