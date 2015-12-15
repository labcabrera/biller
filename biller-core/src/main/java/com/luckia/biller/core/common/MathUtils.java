package com.luckia.biller.core.common;

import java.math.BigDecimal;

/**
 * Clase de utilidades para facilitar el uso de {@link BigDecimal}
 */
public final class MathUtils {

	public static final BigDecimal HUNDRED = new BigDecimal("100.00");
	public static final BigDecimal THOUSAND = new BigDecimal("1000.00");

	/**
	 * Devuelve <code>true</code> cuando el valor recibido es <code>null</code> o cero.
	 * 
	 * @param value
	 * @return
	 */
	public static final boolean isZero(BigDecimal value) {
		return value == null || value.compareTo(BigDecimal.ZERO) == 0;
	}

	public static final boolean isNotZero(BigDecimal value) {
		return !isZero(value);
	}

	/**
	 * Estrictamente menor que cero.
	 */
	public static final boolean isNegative(BigDecimal value) {
		return BigDecimal.ZERO.compareTo(value) > 0L;
	}

	/**
	 * Estrictamente mayor que cero.
	 */
	public static final boolean isPositive(BigDecimal value) {
		return BigDecimal.ZERO.compareTo(value) < 0L;
	}

	/**
	 * Return <code>true</code> if value is greater than zero.
	 * 
	 * @param value
	 * @return
	 */
	public static final boolean isNotZeroPositive(BigDecimal value) {
		return value != null && value.compareTo(BigDecimal.ZERO) > 0;
	}

	public static BigDecimal safeNull(BigDecimal value) {
		return value == null ? BigDecimal.ZERO : value;
	}

	/**
	 * Prevent class instantiation.
	 */
	private MathUtils() {
	}

}
