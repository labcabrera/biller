package com.luckia.biller.core.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.luckia.biller.core.jpa.UUIDSequence;

/**
 * Generalización de los siguientes tipos:
 * <ul>
 * <li>Facturas: {@link Bill}</li>
 * <li>Liquidaciones: {@link Liquidation}</li>
 * <li>Liquidaciones de rappel: {@link RappelLiquidation}</li>
 * </ul>
 * <p>
 * Esta clase define todos los elementos comunes tales como la fecha e intervalo de facturación, emisor y destinatario, estado (borrador, aceptado, enviado, etc), comentarios o
 * importe entre otros.
 * </p>
 */
@Entity
@Table(name = "B_ABSTRACT_BILL")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "TYPE", discriminatorType = DiscriminatorType.CHAR, length = 1)
@SuppressWarnings("serial")
public abstract class AbstractBill implements Serializable, HasState, Auditable {

	@Id
	@Column(name = "ID", length = 36)
	@GeneratedValue(generator = UUIDSequence.UUIDSEQUENCE)
	protected String id;

	/**
	 * Número de factura.<br>
	 * Hasta que la factura no pasa a estado borrador no se genera este valor.
	 */
	@Column(name = "CODE")
	protected String code;

	/** Fecha de la factura */
	@Temporal(TemporalType.DATE)
	@Column(name = "BILL_DATE", nullable = false)
	protected Date billDate;

	/** Inicio del periodo sobre el cual se calcula la factura */
	@Temporal(TemporalType.DATE)
	@Column(name = "DATE_FROM", nullable = false)
	protected Date dateFrom;

	/** Fin del periodo sobre el cual se calcula la factura */
	@Temporal(TemporalType.DATE)
	@Column(name = "DATE_TO", nullable = false)
	protected Date dateTo;

	/** Emisor de la factura */
	@ManyToOne(cascade = CascadeType.DETACH)
	@JoinColumn(name = "SENDER", nullable = false)
	protected LegalEntity sender;

	/** Receptor de la factura */
	@ManyToOne(cascade = CascadeType.DETACH)
	@JoinColumn(name = "RECEIVER", nullable = false)
	protected LegalEntity receiver;

	/**
	 * Define el estado de la factura (borrador, aceptada, enviada, cancelada...)
	 */
	@ManyToOne(cascade = CascadeType.DETACH, fetch = FetchType.EAGER)
	@JoinColumn(name = "CURRENT_STATE")
	protected State currentState;

	/**
	 * Modelo de facturacion utilizado para generar la factura.
	 */
	@ManyToOne(cascade = CascadeType.DETACH, fetch = FetchType.EAGER, optional = false)
	@JoinColumn(name = "MODEL_ID")
	protected BillingModel model;

	@Column(name = "AMOUNT", precision = 18, scale = 2)
	protected BigDecimal amount;

	@Column(name = "COMMENTS", columnDefinition = "TEXT")
	protected String comments;

	@Column(name = "COMMENTS_PDF", columnDefinition = "TEXT")
	protected String commentsPdf;

	@OneToOne(cascade = CascadeType.DETACH)
	protected AppFile pdfFile;

	@Embedded
	protected AuditData auditData;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public Date getBillDate() {
		return billDate;
	}

	public void setBillDate(Date billDate) {
		this.billDate = billDate;
	}

	public Date getDateFrom() {
		return dateFrom;
	}

	public void setDateFrom(Date dateFrom) {
		this.dateFrom = dateFrom;
	}

	public Date getDateTo() {
		return dateTo;
	}

	public void setDateTo(Date dateTo) {
		this.dateTo = dateTo;
	}

	public LegalEntity getSender() {
		return sender;
	}

	public void setSender(LegalEntity sender) {
		this.sender = sender;
	}

	public LegalEntity getReceiver() {
		return receiver;
	}

	public void setReceiver(LegalEntity receiver) {
		this.receiver = receiver;
	}

	@Override
	public State getCurrentState() {
		return currentState;
	}

	@Override
	public void setCurrentState(State currentState) {
		this.currentState = currentState;
	}

	public BillingModel getModel() {
		return model;
	}

	public void setModel(BillingModel model) {
		this.model = model;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	public String getComments() {
		return comments;
	}

	public void setComments(String comments) {
		this.comments = comments;
	}

	public AppFile getPdfFile() {
		return pdfFile;
	}

	public String getCommentsPdf() {
		return commentsPdf;
	}

	public void setCommentsPdf(String commentsPdf) {
		this.commentsPdf = commentsPdf;
	}

	public void setPdfFile(AppFile pdfFile) {
		this.pdfFile = pdfFile;
	}

	@Override
	public AuditData getAuditData() {
		return auditData;
	}

	@Override
	public void setAuditData(AuditData auditData) {
		this.auditData = auditData;
	}

	@SuppressWarnings("unchecked")
	public <T> T getSender(Class<T> type) {
		return (T) sender;
	}

	@SuppressWarnings("unchecked")
	public <T> T getReceiver(Class<T> type) {
		return (T) receiver;
	}
}
