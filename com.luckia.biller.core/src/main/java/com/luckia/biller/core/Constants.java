/*******************************************************************************
 * Copyright (c) 2004, 2012 Kotasoft S.L.
 * All rights reserved. This program and the accompanying materials
 * may only be used prior written consent of Kotasoft S.L.
 ******************************************************************************/
package com.luckia.biller.core;

/**
 * Definición de las constantes de la aplicación.
 */
public final class Constants {

	/**
	 * Luckia Information Server
	 */
	public static final String LIS = "LIS";

	/**
	 * Persistence unit de la aplicación
	 */
	public static final String PERSISTENCE_UNIT_NAME = "com.luckia.biller";

	/**
	 * Persistence unit de la base de datos de LIS
	 */
	public static final String PERSISTENCE_UNIT_NAME_LIS = "com.luckia.lis";

	/**
	 * Nombre del fichero de propiedades de la aplicación
	 */
	public static final String PROPERTIES_FILE = "com.luckia.biller.properties";

	/**
	 * Constructor privado para evitar que se generen instancias de esta clase.
	 */
	private Constants() {
	}

}
