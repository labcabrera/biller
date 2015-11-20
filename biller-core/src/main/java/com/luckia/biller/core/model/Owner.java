package com.luckia.biller.core.model;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * Representa el titular de un establecimiento.
 */
@Entity
@Table(name = "B_OWNER")
@DiscriminatorValue("O")
@SuppressWarnings("serial")
public class Owner extends Person {

}
