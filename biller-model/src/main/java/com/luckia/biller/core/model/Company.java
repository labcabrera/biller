package com.luckia.biller.core.model;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * Representa un operador. Los operadores tienen dos roles:
 * <ul>
 * <li>Reciben las facturas de los establecimientos</li>
 * <li>Realizan las liquidaciones de co-explotación con Luckia</li>
 * </ul>
 */
@Entity
@Table(name = "B_COMPANY")
@DiscriminatorValue("C")
@SuppressWarnings("serial")
public class Company extends LegalEntity {

	@ManyToOne(cascade = CascadeType.DETACH)
	@JoinColumn(name = "BILLING_MODEL_ID")
	private BillingModel billingModel;

	/**
	 * En caso de que este valor esté a <code>true</code> las liquidaciones se aceptarán nada más se genere el borrador, no será necesario que un operador acepte manualmente las
	 * liquidaciones emitidas por esta empresa.
	 */
	@Column(name = "AUTO_CONFIRM")
	private Boolean autoConfirm;

	@Column(name = "LIQUIDATION_SEQUENCE_PREFIX", length = 32)
	private String liquidationSequencePrefix;

	/**
	 * En alguna empresas el pago de las facturas debe estar incluido en la liquidacion.
	 */
	@Column(name = "INCLUDE_STORE_BILLS")
	private Boolean includeStoreBills;

	public BillingModel getBillingModel() {
		return billingModel;
	}

	public void setBillingModel(BillingModel billingModel) {
		this.billingModel = billingModel;
	}

	@Override
	public void merge(LegalEntity entity) {
		if (entity != null) {
			super.merge(entity);
			if (Company.class.isAssignableFrom(entity.getClass())) {
				Company company = (Company) entity;
				this.autoConfirm = company.autoConfirm;
				this.liquidationSequencePrefix = company.liquidationSequencePrefix;
			}
		}
	}

	public Boolean getAutoConfirm() {
		return autoConfirm;
	}

	public void setAutoConfirm(Boolean autoConfirm) {
		this.autoConfirm = autoConfirm;
	}

	public String getLiquidationSequencePrefix() {
		return liquidationSequencePrefix;
	}

	public void setLiquidationSequencePrefix(String liquidationSequencePrefix) {
		this.liquidationSequencePrefix = liquidationSequencePrefix;
	}

	public Boolean getIncludeStoreBills() {
		return includeStoreBills;
	}

	public void setIncludeStoreBills(Boolean includeStoreBills) {
		this.includeStoreBills = includeStoreBills;
	}
}
