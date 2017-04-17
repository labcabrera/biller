package com.luckia.biller.core.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

@Entity
@Table(name = "S_ALERT_RECEIVER")
@Data
@SuppressWarnings("serial")
public class AlertReceiver implements Mergeable<AlertReceiver>, Serializable {

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

	@Override
	public void merge(AlertReceiver entity) {
		this.email = entity.email;
		this.disabled = entity.disabled;
		this.level = entity.level;
	}
}
