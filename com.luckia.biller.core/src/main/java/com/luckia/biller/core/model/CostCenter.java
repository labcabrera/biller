package com.luckia.biller.core.model;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.luckia.biller.core.model.validation.ValidCostCenter;

/**
 * Representa un centro de coste que se utilizara para generar las liquidaciones.<br>
 * En un primer momento habra un unico centro de coste por comunidad autonoma.
 */
@Entity
@Table(name = "B_COST_CENTER")
@DiscriminatorValue("O")
@ValidCostCenter
@SuppressWarnings("serial")
public class CostCenter extends LegalEntity {

	/**
	 * Codigo del centro de coste
	 */
	@Column(name = "CODE", length = 8, nullable = false)
	private String code;

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((code == null) ? 0 : code.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		CostCenter other = (CostCenter) obj;
		if (id == null) {
			if (other.id != null) {
				return false;
			}
		} else if (!id.equals(other.id)) {
			return false;
		}
		return true;
	}
}
