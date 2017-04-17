package com.luckia.biller.core.model;

import java.io.Serializable;
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
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;

import org.eclipse.persistence.annotations.JoinFetch;
import org.eclipse.persistence.annotations.JoinFetchType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lombok.Data;

/**
 * Entidad abstracta que representa una entidad legal. Hay diferentes tipos de entidades
 * legales: personas, establecimientos, empresas, etc.
 */
@Entity
@Table(name = "B_LEGAL_ENTITY", uniqueConstraints = @UniqueConstraint(columnNames = {
		"ID_CARD_NUMBER" }))
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "TYPE", discriminatorType = DiscriminatorType.CHAR, length = 1)
@Data
@SuppressWarnings("serial")
public class LegalEntity implements Serializable, Mergeable<LegalEntity>, Auditable {

	private static final Logger LOG = LoggerFactory.getLogger(LegalEntity.class);

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
	@ManyToOne(cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
	@JoinColumn(name = "ADDRESS_ID")
	@JoinFetch(JoinFetchType.OUTER)
	protected Address address;

	@ManyToOne(cascade = CascadeType.DETACH, fetch = FetchType.LAZY)
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

	@Version
	@Column(name = "VERSION")
	protected Integer version;

	public LegalEntity withName(String name) {
		this.name = name;
		return this;
	}

	@Override
	public String toString() {
		return "LegalEntity [id=" + id + ", idCard=" + idCard + ", name=" + name
				+ ", email=" + email + "]";
	}

	@Override
	public void merge(LegalEntity entity) {
		if (entity == null) {
			throw new NullPointerException("Null reference received in merge operation");
		}
		else if (version != null && entity.version != null
				&& !version.equals(entity.version)) {
			// TODO throw exception instead log
			LOG.warn("Invalid entity version. Current: {}. Received: {}", version,
					entity.version);

		}
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
		this.accountNumber = entity.accountNumber;
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
