package com.luckia.biller.core.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.eclipse.persistence.annotations.Index;

import com.luckia.biller.core.serialization.NotSerializable;

/**
 * Entidad que representa un usuario de la aplicación.
 */
@Entity
@Table(name = "S_USER")
@SuppressWarnings("serial")
@NamedQueries({ @NamedQuery(name = "User.selectByName", query = "select e from User e where e.name = :name") })
public class User implements Serializable {

	@Id
	@GeneratedValue
	@Column(name = "ID")
	private Long id;

	@Column(name = "NAME", unique = true, length = 32, nullable = false)
	@Index(name = "IX_S_USER_NAME")
	private String name;

	@Column(name = "EMAIL", unique = true, length = 128, nullable = false)
	@Index(name = "IX_S_USER_EMAIL")
	private String email;

	@Transient
	private String password;

	@Column(name = "PASSWORD_DIGEST", length = 256, nullable = false)
	@NotSerializable
	private String passwordDigest;

	@Column(name = "created")
	@Temporal(TemporalType.TIMESTAMP)
	private Date created;

	public User() {
	}

	public User(String name, String email, String passwordDigest, Date created) {
		this.name = name;
		this.email = email;
		this.passwordDigest = passwordDigest;
		this.created = created;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPasswordDigest() {
		return passwordDigest;
	}

	public void setPasswordDigest(String passwordDigest) {
		this.passwordDigest = passwordDigest;
	}

	public Date getCreated() {
		return created;
	}

	public void setCreated(Date created) {
		this.created = created;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
}
