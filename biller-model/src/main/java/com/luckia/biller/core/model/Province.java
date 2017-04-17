package com.luckia.biller.core.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.eclipse.persistence.annotations.Index;

import lombok.Data;

/**
 * Representa una provincia
 */
@Entity
@Table(name = "S_PROVINCE")
@Data
@SuppressWarnings("serial")
public class Province implements Serializable {

	@Id
	@Column(name = "ID")
	private String id;

	@Column(name = "CODE", length = 2, nullable = false)
	@Index(name = "IX_PROVINCE_CODE")
	private String code;

	@Column(name = "NAME", length = 36, nullable = false)
	private String name;

	@Override
	public String toString() {
		return name;
	}
}
