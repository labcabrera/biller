package com.luckia.biller.core.model;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.luckia.biller.core.serialization.NotSerializable;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entidad que representa un usuario de la aplicación.
 */
@Entity
@Table(name = "S_USER")
@NamedQueries({
		@NamedQuery(name = "User.selectByAlias", query = "select e from User e where e.alias = :alias") })
@Data
@NoArgsConstructor
@SuppressWarnings("serial")
public class User implements Serializable, Mergeable<User> {

	@Id
	@GeneratedValue
	@Column(name = "ID")
	private Long id;

	@Column(name = "COMPLETE_NAME", unique = true, length = 128, nullable = false)
	private String name;

	@Column(name = "ALIAS", unique = true, length = 128, nullable = false)
	private String alias;

	@Column(name = "EMAIL", unique = true, length = 128, nullable = false)
	private String email;

	@Column(name = "PASSWORD_DIGEST", length = 256, nullable = false)
	@NotSerializable
	private String passwordDigest;

	@Column(name = "created")
	@Temporal(TemporalType.TIMESTAMP)
	private Date created;

	@Column(name = "DISABLED")
	@Temporal(TemporalType.TIMESTAMP)
	private Date disabled;

	@OneToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "S_USER_ROLE_RELATION", joinColumns = {
			@JoinColumn(name = "USER_ID") }, inverseJoinColumns = {
					@JoinColumn(name = "ROLE_ID") })
	private List<UserRole> roles;

	public User(String name, String email, String passwordDigest, Date created) {
		this.name = name;
		this.email = email;
		this.passwordDigest = passwordDigest;
		this.created = created;
	}

	@Override
	public void merge(User entity) {
		if (entity != null) {
			this.alias = entity.alias;
			this.name = entity.name;
			this.email = entity.email;
			this.roles = entity.roles;
		}
	}
}
