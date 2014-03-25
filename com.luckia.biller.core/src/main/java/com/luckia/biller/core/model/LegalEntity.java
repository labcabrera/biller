/*******************************************************************************
 * Copyright (c) 2004, 2012 Kotasoft S.L.
 * All rights reserved. This program and the accompanying materials
 * may only be used prior written consent of Kotasoft S.L.
 ******************************************************************************/
package com.luckia.biller.core.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;

import com.luckia.biller.core.jpa.Mergeable;
import com.luckia.biller.core.model.validation.ValidLegalEntity;

/**
 * Entidad abstracta que representa una entidad legal. Hay diferentes tipos de entidades legales: personas, establecimientos, empresas, etc.
 */
@Entity
@Table(name = "B_LEGAL_ENTITY", uniqueConstraints = @UniqueConstraint(columnNames = { "ID_CARD_NUMBER" }))
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "TYPE", discriminatorType = DiscriminatorType.CHAR, length = 1)
@SuppressWarnings("serial")
@ValidLegalEntity
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
	protected Address address;

	@ManyToOne(cascade = CascadeType.PERSIST)
	@JoinColumn(name = "PARENT_ID")
	protected LegalEntity parent;

	@Lob
	@Basic(fetch = FetchType.LAZY)
	@Column(name = "COMMENTS", columnDefinition = "BLOB")
	protected String comments;

	@Column(name = "CREATED")
	@Temporal(TemporalType.TIMESTAMP)
	private Date created;

	@Column(name = "DELETED")
	@Temporal(TemporalType.TIMESTAMP)
	private Date deleted;

	@Column(name = "MODIFIED")
	@Temporal(TemporalType.TIMESTAMP)
	private Date modified;

	@ManyToOne(cascade = CascadeType.DETACH)
	@JoinColumn(name = "MODIFIED_BY")
	private User modifiedBy;

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

	public Date getCreated() {
		return created;
	}

	public void setCreated(Date created) {
		this.created = created;
	}

	public Date getDeleted() {
		return deleted;
	}

	public void setDeleted(Date deleted) {
		this.deleted = deleted;
	}

	public Date getModified() {
		return modified;
	}

	public void setModified(Date modified) {
		this.modified = modified;
	}

	public User getModifiedBy() {
		return modifiedBy;
	}

	public void setModifiedBy(User modifiedBy) {
		this.modifiedBy = modifiedBy;
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
		if (this.parent == null && entity.parent != null) {
			this.parent = entity.parent;
		}
		if (this.idCard == null && entity.idCard != null) {
			this.idCard = new IdCard();
		}
		this.name = entity.name;
		this.email = entity.email;
		this.comments = entity.comments;
		this.phoneNumber = entity.phoneNumber;
		if (this.address != null) {
			this.address.merge(entity.getAddress());
		}
		if (this.idCard != null) {
			this.idCard.merge(entity.getIdCard());
		}
		if (this.parent != null) {
			this.parent.merge(entity.parent);
		}
	}
}
