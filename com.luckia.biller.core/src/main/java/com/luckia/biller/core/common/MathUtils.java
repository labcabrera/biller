package com.luckia.biller.core.common;

import java.math.BigDecimal;

/**
 * Clase de utilidades para facilitar el uso de {@link BigDecimal}
 */
public final class MathUtils {

	public static final BigDecimal HUNDRED = new BigDecimal("100.00");

	/**
	 * Devuelve <code>true</code> cuando el valor recibido es <code>null</code>
	 * o cero.
	 * 
	 * @param value
	 * @return
	 */
	public static final boolean isZero(BigDecimal value) {
		return value == null || value.compareTo(BigDecimal.ZERO) == 0;
	}

	/**
	 * Devuelve <code>true</code> cuando el valor recibido es superior a cero.
	 * 
	 * @param value
	 * @return
	 */
	public static final boolean isNotZeroPositive(BigDecimal value) {
		return value != null && value.compareTo(BigDecimal.ZERO) > 0;
	}

	private MathUtils() {
	}
}
