package com.luckia.biller.core.model;

/**
 * Tipo enumerado que establece los diferentes tipos de establecimiento
 * 
 */
public enum StoreType {

	Bar("Bar"),

	SalonCorner("Córner en Salón"), CasinoCorner("Córner en casino"), BingoCorner(
			"Córner en bingo"),

	SpecificStore("Establecimiento específico"), LSC("Luckia Sport Café"), CasaDeApuestas(
			"Casa de apuestas");

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
