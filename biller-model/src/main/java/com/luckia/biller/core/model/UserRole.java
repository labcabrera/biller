package com.luckia.biller.core.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "S_USER_ROLE")
public class UserRole {

	public static final String ADMIN = "Administrador";
	public static final String OPERATOR = "Operador";
	public static final String READ_ONLY = "Lectura";

	@Id
	@Column(name = "ID")
	private Long id;

	@Column(name = "NAME", length = 32)
	private String name;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
