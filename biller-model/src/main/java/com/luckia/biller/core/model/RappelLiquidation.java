package com.luckia.biller.core.model;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.luckia.biller.core.serialization.NotSerializable;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Entidad que representa el conjunto de bonus por rappel a todos los establecimeintos de
 * un operador.
 */
@Entity
@Table(name = "B_RAPPEL_LIQUIDATION")
@DiscriminatorValue("R")
@Data
@EqualsAndHashCode(callSuper = true)
@SuppressWarnings("serial")
public class RappelLiquidation extends AbstractBill {

	/**
	 * Lista de bonus de cada establecimiento.
	 */
	@OneToMany(cascade = CascadeType.PERSIST, fetch = FetchType.LAZY, mappedBy = "rappelLiquidation")
	@NotSerializable
	private List<RappelStoreBonus> storeBonus;

}
