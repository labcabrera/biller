package com.luckia.biller.core.services.bills;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.Range;

import com.luckia.biller.core.model.Bill;
import com.luckia.biller.core.model.BillConcept;

/**
 * Interfaz que nos permite acceder a los datos de facturación de LIS.<br>
 * En principio los datos se obtendran a partir de consultas a la base de datos de LIS (MySQL) en las que previamente se habran consolidado
 * mensualmente los resultados de cada terminal. Deberiamos tener una tabla con la siguiente estructura:
 * <ul>
 * <li>TERMINAL_ID: identificador del terminal</li>
 * <li>TERMINAL_LOCATION: nombre que identifica los datos del terminal</li>
 * <li>VENTAS_TOTALES: sumatorio de todas las ventas del terminal en el intervalo indicado</li>
 * <li>CANCELACIONES_TOTALES: sumatorio de todas las cancelaciones de tickets</li>
 * <li>IMPORTE_PAGADO: total de premios pagados por el terminal</li>
 * </ul>
 */
public interface BillDataProvider {

	/**
	 * Obtiene la tabla con los datos de facturacion para una lista determinada de terminales y un rango de fechas.
	 * 
	 * @param concept
	 * @param range
	 * @param terminals
	 * @return
	 */
	Map<BillConcept, BigDecimal> retreive(Bill bill, Range<Date> range, List<String> terminals);

}