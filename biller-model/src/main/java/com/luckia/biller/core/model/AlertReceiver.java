package com.luckia.biller.core.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "S_ALERT_RECEIVER")
public class AlertReceiver implements Mergeable<AlertReceiver> {

	@Id
	@Column(name = "ID", length = 32)
	@GeneratedValue(generator = "system-uuid")
	private String id;

	@Column(name = "EMAIL", nullable = false, length = 256)
	private String email;

	@Column(name = "LEVEL", nullable = false, length = 8)
	@Enumerated(EnumType.STRING)
	private AlertLevel level;

	@Column(name = "DISABLED", nullable = false)
	private Boolean disabled;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public AlertLevel getLevel() {
		return level;
	}

	public void setLevel(AlertLevel level) {
		this.level = level;
	}

	public Boolean getDisabled() {
		return disabled;
	}

	public void setDisabled(Boolean disabled) {
		this.disabled = disabled;
	}

	@Override
	public void merge(AlertReceiver entity) {
		this.email = entity.email;
		this.disabled = entity.disabled;
		this.level = entity.level;
	}
}
