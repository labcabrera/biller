package com.luckia.biller.core.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import lombok.Data;

@Entity
@Table(name = "S_USER_ACTIVITY")
@Data
@SuppressWarnings("serial")
public class UserActivity implements Serializable {

	@Id
	@Column(name = "ID")
	private String id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "USER_ID", nullable = false)
	private User user;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "ACTIVITY_DATE", nullable = false)
	private Date date;

	@Column(name = "TYPE", length = 128, nullable = false)
	@Enumerated(EnumType.STRING)
	private UserActivityType type;

	@Column(name = "DATA", columnDefinition = "TEXT", nullable = true)
	private String data;

}
