package com.luckia.biller.core.model;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Table;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Representa un centro de coste que se utilizara para generar las liquidaciones.<br>
 * En un primer momento habra un unico centro de coste por comunidad autonoma.
 */
@Entity
@Table(name = "B_COST_CENTER")
@DiscriminatorValue("O")
@Data
@EqualsAndHashCode(callSuper = true)
@SuppressWarnings("serial")
public class CostCenter extends LegalEntity {

	/**
	 * Codigo del centro de coste
	 */
	@Column(name = "CODE", length = 8)
	private String code;

	@Override
	public void merge(LegalEntity entity) {
		if (entity != null) {
			super.merge(entity);
			if (CostCenter.class.isAssignableFrom(entity.getClass())) {
				CostCenter costCenter = (CostCenter) entity;
				code = costCenter.code;
			}
		}
	}
}
