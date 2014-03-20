/*******************************************************************************
 * Copyright (c) 2004, 2012 Kotasoft S.L.
 * All rights reserved. This program and the accompanying materials
 * may only be used prior written consent of Kotasoft S.L.
 ******************************************************************************/
package com.luckia.biller.core.model;

/**
 * Tipo enumerado que establece los diferentes tipos de establecimiento
 * 
 * @author lab
 * 
 */
public enum StoreType {

	Bar("Bar"),

	SalonCorner("Córner en Salón"), CasinoCorner("Córner en casino"), BingoCorner("Córner en bingo"),

	SpecifigStore("Establecimiento específico"), LSC("LCS"), CasaDeApuestas("Casa de apuestas");

	private String desc;

	private StoreType(String desc) {
		this.desc = desc;
	}

	/**
	 * Obtiene el literal asociado al estado.
	 * 
	 * @return
	 */
	public String desc() {
		return desc;
	}

}
