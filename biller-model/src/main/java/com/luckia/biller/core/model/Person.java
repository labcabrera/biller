package com.luckia.biller.core.model;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Table;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Representa una persona
 */
@Entity
@Table(name = "B_PERSON")
@DiscriminatorValue("P")
@Data
@EqualsAndHashCode(callSuper = true)
@SuppressWarnings("serial")
public class Person extends LegalEntity {

	@Column(name = "FIRST_SURNAME", length = 32)
	private String firstSurname;

	@Column(name = "SECOND_SURNAME", length = 32)
	private String secondSurname;

	@Override
	public void merge(LegalEntity entity) {
		if (entity != null) {
			super.merge(entity);
			if (Person.class.isAssignableFrom(entity.getClass())) {
				Person person = (Person) entity;
				this.firstSurname = person.firstSurname;
				this.secondSurname = person.secondSurname;
			}
		}
	}
}
