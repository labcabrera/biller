package com.luckia.biller.core.model;

import java.io.Serializable;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.MappedSuperclass;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.luckia.biller.core.serialization.NotSerializable;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Representa una liquidación. A diferencia de las facturas las liquidaciones no devengan
 * IVA. Las liquidaciones se realizan a las empresas como un agregado de todas las
 * facturas emitidas a los establecimientos pertenecientes a la empresa.<br>
 * Para cada empresa operadora se generan n facturas dependiendo de los centros de coste a
 * los que estén asociados los establecimientos. Por ejemplo, si una empresa opera en
 * Galicia y Valencia, se generarán dos liquidaciones para esa empresa, una para cada
 * comunidad autónoma.
 * <ul>
 * <li>El emisor de la factura será la empresa operadora</li>
 * <li>El receptor de la factura será el centro de coste</li>
 * </ul>
 * 
 */
@Entity
@Table(name = "B_LIQUIDATION")
@DiscriminatorValue("L")
@MappedSuperclass
@NamedQueries({
		@NamedQuery(name = "Liquidation.selectByCompanyInRange", query = "select e from Liquidation e where e.sender = :sender and e.dateFrom >= :from and e.dateTo <= :to") })
@Data
@EqualsAndHashCode(callSuper = true)
@SuppressWarnings("serial")
public class Liquidation extends AbstractBill
		implements Mergeable<Liquidation>, Serializable {

	public static final String QUERY_SEARCH_BY_COMPANY_IN_RANGE = "Liquidation.selectByCompanyInRange";

	/**
	 * Lista de facturas que componen la liquidación.
	 */
	@OneToMany(cascade = CascadeType.PERSIST, fetch = FetchType.LAZY, mappedBy = "liquidation")
	@NotSerializable
	private List<Bill> bills;

	@Embedded
	private LiquidationResults liquidationResults;

	/**
	 * Version con la que se ha generado la liquidacion (utilizado para migraciones)
	 */
	@Column(name = "MODEL_VERSION", length = 8)
	private String modelVersion;

	/**
	 * Lista de ajustes incluidos dentro de laliquidación.
	 */
	@OneToMany(cascade = CascadeType.PERSIST, fetch = FetchType.LAZY, mappedBy = "liquidation")
	private List<LiquidationDetail> details;

	@Override
	public void merge(Liquidation entity) {
		this.billDate = entity.billDate;
		this.comments = entity.comments;
		this.commentsPdf = entity.commentsPdf;
		this.modelVersion = entity.modelVersion;
		if (entity.liquidationResults != null) {
			if (this.liquidationResults == null) {
				this.liquidationResults = new LiquidationResults();
			}
			this.liquidationResults.merge(entity.liquidationResults);
		}
		else {
			this.liquidationResults = null;
		}
	}

	public boolean vatApplies(BillingModel model) {
		boolean hasVat = false;
		if (model != null && model.getVatLiquidationType() != null) {
			switch (model.getVatLiquidationType()) {
			case LIQUIDATION_ADDED:
			case LIQUIDATION_INCLUDED:
				hasVat = true;
				break;
			default:
				break;
			}
		}
		return hasVat;
	}

	@Override
	public BillingModel getModel() {
		if (model != null) {
			return model;
		}
		else if (bills != null) {
			for (Bill bill : bills) {
				if (bill.getModel() != null) {
					return bill.getModel();
				}
			}
		}
		return null;
	}

	@Override
	public String toString() {
		return String.format("Liquidation/%s/%s",
				sender != null ? sender.getName() : null, id);
	}
}
