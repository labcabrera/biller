package com.luckia.biller.core.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;

import org.eclipse.persistence.annotations.ChangeTracking;
import org.eclipse.persistence.annotations.ChangeTrackingType;
import org.eclipse.persistence.annotations.JoinFetch;
import org.eclipse.persistence.annotations.JoinFetchType;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Entidad que representa una factura. La aplicacion genera las facturas que emiten los
 * establecimientos a sus operadores de máquinas de juego.
 * <ul>
 * <li>En emisor de la factura será el titular del establecimiento.</li>
 * <li>El receptor de la factura será la empresa a la que está asociada el
 * establecimiento</li>
 * <li>El código de la factura se generará secuencialmente a partir de la secuencia de
 * cada uno de los clientes. El código solo se generará una vez se aprueba la factura</li>
 * </ul>
 * Los detalles de las facturas tienen tres posibilidades:
 * <ul>
 * <li><b>Ajustes operativos</b> (incluidos en la liquidacion): los ajustes operativos
 * representan robos o descuadres que practicamente siempre serán negativos.</li>
 * <li><b>Ajustes manuales incluídos en la liquidación</b></li>
 * <li><b>Ajustes manuales NO incluídos en la liquidación</b></li>
 * </ul>
 */
@Entity
@Table(name = "B_BILL")
@DiscriminatorValue("B")
@SuppressWarnings("serial")
@ChangeTracking(ChangeTrackingType.DEFERRED)
@NamedQueries({
		@NamedQuery(name = "Bill.selectPendingByReceiverInRange", query = "select b from Bill b where b.receiver = :receiver and b.dateFrom >= :from and b.dateTo <= :to and b.liquidation is null"),
		@NamedQuery(name = "Bill.selectByStoreInRange", query = "select b from Bill b where b.sender = :sender and b.dateFrom >= :from and b.dateTo <= :to") })
@Data
@EqualsAndHashCode(callSuper = true)
public class Bill extends AbstractBill implements Mergeable<Bill>, Serializable {

	/**
	 * Determinadas facturas pueden tener una relación padre-hijo con otras (por ejemplo
	 * las rectificaciones). Este atributo nos permite definir esta relación.
	 */
	@ManyToOne(cascade = CascadeType.DETACH)
	@JoinColumn(name = "PARENT_ID")
	private Bill parent;

	/**
	 * Tipo de la factura (ordinaria o rectificativa)
	 */
	@Column(name = "TYPE", length = 16, nullable = false)
	@Enumerated(EnumType.STRING)
	private BillType billType;

	/**
	 * Lista de detalles que componen la factura
	 */
	@OneToMany(cascade = CascadeType.ALL, mappedBy = "bill")
	@OrderBy("value DESC")
	private List<BillDetail> billDetails;

	/**
	 * Lista de detalles que componen la liquidacion
	 */
	@OneToMany(cascade = CascadeType.ALL, mappedBy = "bill")
	@OrderBy("value DESC")
	private List<BillLiquidationDetail> liquidationDetails;

	@Column(name = "NET_AMOUNT", precision = 18, scale = 2)
	private BigDecimal netAmount;

	@Column(name = "VAT_AMOUNT", precision = 18, scale = 2)
	private BigDecimal vatAmount;

	@Column(name = "VAT_PERCENT", precision = 18, scale = 2)
	private BigDecimal vatPercent;

	@Column(name = "LIQUIDATION_TOTAL_AMOUNT", precision = 18, scale = 2)
	private BigDecimal liquidationTotalAmount;

	@Column(name = "LIQUIDATION_TOTAL_VAT", precision = 18, scale = 2)
	private BigDecimal liquidationTotalVat;

	@Column(name = "LIQUIDATION_TOTAL_NET_AMOUNT", precision = 18, scale = 2)
	private BigDecimal liquidationTotalNetAmount;

	@Column(name = "LIQUIDATION_BET_AMOUNT", precision = 18, scale = 2)
	private BigDecimal liquidationBetAmount;

	@Column(name = "LIQUIDATION_SAT_AMOUNT", precision = 18, scale = 2)
	private BigDecimal liquidationSatAmount;

	@Column(name = "LIQUIDATION_PRICE_PER_LOCATION", precision = 18, scale = 2)
	private BigDecimal liquidationPricePerLocation;

	@Column(name = "LIQUIDATION_MANUAL_AMOUNT", precision = 18, scale = 2)
	private BigDecimal liquidationManualAmount;

	@Column(name = "LIQUIDATION_OUTER_AMOUNT", precision = 18, scale = 2)
	private BigDecimal liquidationOuterAmount;

	@ManyToOne(cascade = CascadeType.DETACH)
	@JoinColumn(name = "LIQUIDATION_ID")
	private Liquidation liquidation;

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "bill", cascade = CascadeType.PERSIST)
	private List<BillRawData> billRawData;

	/**
	 * Representa el saldo de caja del establecimiento (pagos - cancelaciones - premios)
	 */
	@Column(name = "STORE_CASH", precision = 18, scale = 2)
	private BigDecimal storeCash;

	@ManyToOne(cascade = CascadeType.DETACH, optional = true)
	@JoinTable(name = "B_BILL_LIQUIDATION_FILE", joinColumns = @JoinColumn(name = "BILL_ID"), inverseJoinColumns = @JoinColumn(name = "FILE_ID"))
	@JoinFetch(JoinFetchType.OUTER)
	private AppFile liquidationDetailFile;

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("Bill(code: %s, state: %s, amount: %s)", code, currentState,
				amount);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.luckia.biller.core.jpa.Mergeable#merge(java.lang.Object)
	 */
	@Override
	public void merge(Bill entity) {
		this.billDate = entity.billDate;
		this.comments = entity.comments;
		this.commentsPdf = entity.commentsPdf;
		this.receiver = entity.receiver;
		this.code = entity.code;
		this.model = entity.model;
	}
}
