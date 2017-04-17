package com.luckia.biller.core.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

@Entity
@Table(name = "S_USER_ROLE")
@SuppressWarnings("serial")
@Data
public class UserRole implements Serializable {

	public static final String CODE_ADMIN = "ADMIN";
	public static final String CODE_OPERATOR = "OPERATOR";
	public static final String CODE_READ_ONLY = "READ_ONLY";

	@Id
	@Column(name = "ID")
	private Long id;

	@Column(name = "CODE", length = 32)
	private String code;

}
