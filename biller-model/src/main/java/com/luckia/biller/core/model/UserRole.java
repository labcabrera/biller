package com.luckia.biller.core.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "S_USER_ROLE")
public class UserRole {

	public static final String CODE_ADMIN = "ADMIN";
	public static final String CODE_OPERATOR = "OPERATOR";
	public static final String CODE_READ_ONLY = "READ_ONLY";

	@Id
	@Column(name = "ID")
	private Long id;

	@Column(name = "CODE", length = 32)
	private String code;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}
}
