/*******************************************************************************
 * Copyright (c) 2004, 2012 Kotasoft S.L.
 * All rights reserved. This program and the accompanying materials
 * may only be used prior written consent of Kotasoft S.L.
 ******************************************************************************/
package com.luckia.biller.core.model;

import java.io.Serializable;
import java.util.List;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;

import com.luckia.biller.core.jpa.Mergeable;

/**
 * Entidad que establece la parametrización con la que se van a generar tanto facturas como liquidaciones.
 */
@Entity
@Table(name = "B_BILLING_MODEL")
@NamedQueries({ @NamedQuery(name = "BillingModel.selectAll", query = "select e from BillingModel e order by e.name") })
@SuppressWarnings("serial")
public class BillingModel implements Serializable, Mergeable<BillingModel>, Auditable {

	@Id
	@GeneratedValue
	@Column(name = "ID")
	private Long id;

	@Column(name = "NAME", length = 256, nullable = false)
	private String name;

	@Embedded
	@AttributeOverrides({ @AttributeOverride(name = "stakesPercent", column = @Column(name = "STORE_STAKES_PERCENT", precision = 6, scale = 2)),
			@AttributeOverride(name = "ggrPercent", column = @Column(name = "STORE_GGR_PERCENT", precision = 6, scale = 2)),
			@AttributeOverride(name = "ngrPercent", column = @Column(name = "STORE_NGR_PERCENT", precision = 6, scale = 2)),
			@AttributeOverride(name = "nrPercent", column = @Column(name = "STORE_NR_PERCENT", precision = 6, scale = 2)),
			@AttributeOverride(name = "coOperatingMonthlyFees", column = @Column(name = "STORE_COOPERATING_FEES", precision = 18, scale = 2)),
			@AttributeOverride(name = "commercialMonthlyFees", column = @Column(name = "STORE_COMMERCIAL_FEES", precision = 18, scale = 2)),
			@AttributeOverride(name = "satMonthlyFees", column = @Column(name = "STORE_SAT_FEES", precision = 18, scale = 2)) })
	private BillingModelAttributes storeModel;

	@Embedded
	@AttributeOverrides({ @AttributeOverride(name = "stakesPercent", column = @Column(name = "COMPANY_STAKES_PERCENT", precision = 6, scale = 2)),
			@AttributeOverride(name = "ggrPercent", column = @Column(name = "COMPANY_GGR_PERCENT", precision = 6, scale = 2)),
			@AttributeOverride(name = "ngrPercent", column = @Column(name = "COMPANY_NGR_PERCENT", precision = 6, scale = 2)),
			@AttributeOverride(name = "nrPercent", column = @Column(name = "COMPANY_NR_PERCENT", precision = 6, scale = 2)),
			@AttributeOverride(name = "coOperatingMonthlyFees", column = @Column(name = "COMPANY_COOPERATING_FEES", precision = 18, scale = 2)),
			@AttributeOverride(name = "commercialMonthlyFees", column = @Column(name = "COMPANY_COMMERCIAL_FEES", precision = 18, scale = 2)),
			@AttributeOverride(name = "satMonthlyFees", column = @Column(name = "COMPANY_SAT_FEES", precision = 18, scale = 2)) })
	private BillingModelAttributes companyModel;

	@OneToMany(cascade = CascadeType.PERSIST, fetch = FetchType.EAGER, mappedBy = "model")
	@OrderBy("amount")
	private List<Rappel> rappel;

	@Embedded
	private AuditData auditData;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<Rappel> getRappel() {
		return rappel;
	}

	public void setRappel(List<Rappel> rappel) {
		this.rappel = rappel;
	}

	@Override
	public AuditData getAuditData() {
		return auditData;
	}

	public BillingModelAttributes getStoreModel() {
		return storeModel;
	}

	public void setStoreModel(BillingModelAttributes storeModel) {
		this.storeModel = storeModel;
	}

	public BillingModelAttributes getCompanyModel() {
		return companyModel;
	}

	public void setCompanyModel(BillingModelAttributes companyModel) {
		this.companyModel = companyModel;
	}

	@Override
	public void setAuditData(AuditData auditData) {
		this.auditData = auditData;
	}

	@Override
	public void merge(BillingModel entity) {
		if (entity != null) {
			if (entity.companyModel == null) {
				companyModel = null;
			} else {
				if (companyModel == null) {
					companyModel = new BillingModelAttributes();
				}
				companyModel.merge(entity.companyModel);
			}
			if (entity.storeModel == null) {
				storeModel = null;
			} else {
				if (storeModel == null) {
					storeModel = new BillingModelAttributes();
				}
				storeModel.merge(entity.storeModel);
			}
			this.name = entity.name;
		}
	}
}
