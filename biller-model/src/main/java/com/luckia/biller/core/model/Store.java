package com.luckia.biller.core.model;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderBy;
import javax.persistence.Table;

import com.luckia.biller.core.serialization.NotSerializable;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Representa un establecimiento.
 */
@Entity
@Table(name = "B_STORE")
@DiscriminatorValue("S")
@NamedQueries({
		@NamedQuery(name = "Store.selectAll", query = "select s from Store s order by s.name"),
		@NamedQuery(name = "Store.selectByCompany", query = "select s from Store s where s.parent = :company order by s.name") })
@Data
@EqualsAndHashCode(callSuper = true)
@SuppressWarnings("serial")
public class Store extends LegalEntity {

	public static final String QUERY_SELECT_ALL = "Store.selectAll";
	public static final String QUERY_SELECT_BY_COMPANY = "Store.selectByCompany";

	@Column(name = "TYPE", length = 32)
	@Enumerated(EnumType.STRING)
	private StoreType type;

	@ManyToOne(cascade = CascadeType.MERGE, fetch = FetchType.EAGER, targetEntity = Owner.class)
	@JoinColumn(name = "OWNER_ID")
	private LegalEntity owner;

	@ManyToOne(cascade = CascadeType.DETACH)
	@JoinColumn(name = "COST_CENTER_ID")
	private CostCenter costCenter;

	@OneToOne(cascade = CascadeType.PERSIST)
	@JoinColumn(name = "ISP_INFO_ID")
	private IspInfo ispInfo;

	@Column(name = "BILL_SEQUENCE_PREFIX", length = 32)
	private String billSequencePrefix;

	@OneToMany(cascade = CascadeType.PERSIST, fetch = FetchType.EAGER, mappedBy = "store")
	@OrderBy("code")
	@NotSerializable
	private List<TerminalRelation> terminalRelations;

	/**
	 * En caso de que este valor esté a <code>true</code> las facturas se aceptarán nada
	 * más se genere el borrador, no será necesario que un operador acepte manualmente la
	 * factura.
	 */
	@Column(name = "AUTO_CONFIRM")
	private Boolean autoConfirm;

	/**
	 * Posibilidades: usar un oneToOne, usar un ManyToOne y dejarlo a null cuando tenga la
	 * facturacion por defecto o cualquier otra alternativa
	 */
	@OneToOne(cascade = CascadeType.DETACH, fetch = FetchType.LAZY)
	@JoinColumn(name = "BILLING_MODEL_ID")
	private BillingModel billingModel;

	@Override
	public void merge(LegalEntity entity) {
		if (entity != null) {
			super.merge(entity);
			if (Store.class.isAssignableFrom(entity.getClass())) {
				Store store = (Store) entity;
				this.type = store.type;
				this.owner = store.owner;
				this.costCenter = store.costCenter;
				this.ispInfo = store.ispInfo;
				this.autoConfirm = store.autoConfirm;
				this.billingModel = store.billingModel;
				this.billSequencePrefix = store.billSequencePrefix;
			}
		}
	}

}
