package com.luckia.biller.core.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import lombok.Data;

@Entity
@Table(name = "S_USER_SESSION")
@Data
@SuppressWarnings("serial")
public class UserSession implements Serializable {

	@Id
	@Column(name = "SESSION", length = 36)
	private String session;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "USER_ID", nullable = false, updatable = false)
	private User user;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "CREATED", nullable = false, updatable = false)
	private Date created;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "LAST_ACCESS", nullable = false, updatable = false)
	private Date lastAccess;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "EXPIRATION", nullable = true, updatable = true)
	private Date expiration;

}
