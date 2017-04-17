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
import lombok.NoArgsConstructor;

/**
 * Representa el numero de identificacion fiscal (NIF o CIF) asociado a una entidad legal.
 */
@Entity
@Table(name = "S_IDCARD")
@Data
@NoArgsConstructor
@SuppressWarnings("serial")
public class IdCard implements Serializable, Mergeable<IdCard> {

	@Id
	@GeneratedValue
	private Long id;

	@Enumerated(EnumType.STRING)
	@Column(name = "ID_CARD_TYPE")
	private IdCardType type;

	@Column(name = "ID_CARD_NUMBER")
	private String number;

	public IdCard(IdCardType type, String number) {
		this.type = type;
		this.number = number;
	}

	@Override
	public String toString() {
		return "IdCard [type=" + type + ", number=" + number + "]";
	}

	@Override
	public void merge(IdCard entity) {
		if (entity != null) {
			this.number = entity.number;
			this.type = entity.type;
		}
	}
}
