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
	 * Persistence unit name de la aplicación
	 */
	public static final String PERSISTENCE_UNIT_NAME = "com.luckia.biller";

	/**
	 * Persistence unit name de la base de datos de LIS
	 */
	public static final String PERSISTENCE_UNIT_NAME_LIS = "com.luckia.lis";

	/**
	 * Nombre del fichero de configuracion de la aplicación
	 */
	public static final String APP_CONFIG_FILE = "biller.config";

	public static final String CONFIG_SECTION_GLOBAL = "global";
	public static final String CONFIG_SECTION_JPA_BILLER = "jpa-biller";
	public static final String CONFIG_SECTION_JPA_LIS = "jpa-lis";

	public static final String I18N_BUNDLE = "i18n";

	/**
	 * Constructor privado para evitar que se generen instancias de esta clase.
	 */
	private Constants() {
	}

}
