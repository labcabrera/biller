package com.luckia.biller.core.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

/**
 * Entidad que representa la información de conexión a internet de un establecimiento.
 */
@Entity
@Table(name = "B_ISO_INFO")
@Data
@SuppressWarnings("serial")
public class IspInfo implements Serializable {

	@Id
	@GeneratedValue
	private Long id;

	@Column(name = "CONNECTION_TYPE", length = 64)
	public String connectionType;

	@Column(name = "TRANSFER_RATE", length = 64)
	public String transferRate;

	@Column(name = "STATUS", length = 64)
	public String status;

	@Column(name = "PHONE_NUMBER", length = 64)
	public String phoneNumber;

	@Column(name = "ISSUE_PHONE_NUMBER", length = 64)
	public String issuePhoneNumber;

}
