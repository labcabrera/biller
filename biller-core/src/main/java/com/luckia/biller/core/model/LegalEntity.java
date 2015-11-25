package com.luckia.biller.core.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;

import org.eclipse.persistence.annotations.JoinFetch;
import org.eclipse.persistence.annotations.JoinFetchType;

/**
 * Entidad abstracta que representa una entidad legal. Hay diferentes tipos de entidades legales: personas, establecimientos, empresas, etc.
 */
@Entity
@Table(name = "B_LEGAL_ENTITY", uniqueConstraints = @UniqueConstraint(columnNames = { "ID_CARD_NUMBER" }) )
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "TYPE", discriminatorType = DiscriminatorType.CHAR, length = 1)
@SuppressWarnings("serial")
// @ValidLegalEntity
@NamedQueries({ @NamedQuery(name = "LegalEntity.selectByName", query = "select e from LegalEntity e where e.name = :name") })
public class LegalEntity implements Serializable, Mergeable<LegalEntity>, Auditable {

	@Id
	@Column(name = "ID")
	@GeneratedValue
	protected Long id;

	@ManyToOne(cascade = CascadeType.PERSIST)
	@JoinColumn(name = "ID_CARD")
	protected IdCard idCard;

	@Column(name = "NAME")
	protected String name;

	@Column(name = "EMAIL")
	protected String email;

	@Column(name = "PHONE_NUMBER")
	protected String phoneNumber;

	/** Fiscal address */
	@ManyToOne(cascade = CascadeType.PERSIST)
	@JoinColumn(name = "ADDRESS_ID")
	@JoinFetch(JoinFetchType.OUTER)
	protected Address address;

	@ManyToOne(cascade = CascadeType.DETACH)
	@JoinColumn(name = "PARENT_ID")
	protected LegalEntity parent;

	@Column(name = "COMMENTS", columnDefinition = "TEXT")
	protected String comments;

	@Column(name = "ACCOUNT_NUMBER", length = 64)
	protected String accountNumber;

	@Temporal(TemporalType.DATE)
	@Column(name = "START_DATE")
	protected Date startDate;

	@Temporal(TemporalType.DATE)
	@Column(name = "END_DATE")
	protected Date endDate;

	@Embedded
	protected AuditData auditData;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public IdCard getIdCard() {
		return idCard;
	}

	public void setIdCard(IdCard idCard) {
		this.idCard = idCard;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public Address getAddress() {
		return address;
	}

	public void setAddress(Address address) {
		this.address = address;
	}

	public LegalEntity getParent() {
		return parent;
	}

	public void setParent(LegalEntity parent) {
		this.parent = parent;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public String getComments() {
		return comments;
	}

	public void setComments(String comments) {
		this.comments = comments;
	}

	public AuditData getAuditData() {
		return auditData;
	}

	public void setAuditData(AuditData auditData) {
		this.auditData = auditData;
	}

	public String getAccountNumber() {
		return accountNumber;
	}

	public void setAccountNumber(String accountNumber) {
		this.accountNumber = accountNumber;
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public LegalEntity withName(String name) {
		this.name = name;
		return this;
	}

	@Override
	public String toString() {
		return "LegalEntity [id=" + id + ", idCard=" + idCard + ", name=" + name + ", email=" + email + "]";
	}

	@Override
	public void merge(LegalEntity entity) {
		if (this.address == null && entity.address != null) {
			this.address = new Address();
		}
		if (this.idCard == null && entity.idCard != null) {
			this.idCard = new IdCard();
		}
		if (this.idCard == null && entity.idCard != null) {
			this.idCard = new IdCard();
		}
		this.name = entity.name;
		this.email = entity.email;
		this.comments = entity.comments;
		this.phoneNumber = entity.phoneNumber;
		this.parent = entity.parent;
		this.startDate = entity.startDate;
		this.endDate = entity.endDate;
		if (this.address != null) {
			this.address.merge(entity.getAddress());
		}
		if (this.idCard != null) {
			this.idCard.merge(entity.getIdCard());
		}
	}

	@SuppressWarnings("unchecked")
	public <T> T as(Class<T> class1) {
		return (T) this;
	}
}
