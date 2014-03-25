package com.luckia.biller.core.services.bills;

import java.util.Date;

import org.apache.commons.lang3.Range;

import com.luckia.biller.core.model.Bill;
import com.luckia.biller.core.model.Store;

/**
 * Define la interface del servicio encargado de generar los detalles de una factura y calcular su importe.
 */
public interface BillProcessor {

	/**
	 * Crea, inicializa y persiste la factura.
	 * 
	 * @param store
	 * @param range
	 * @return
	 */
	Bill generateBill(Store store, Range<Date> range);

	/**
	 * Crea la lista de detalles de facturacion a partir del modelo de facturacion utilizado.
	 * 
	 * @param bill
	 */
	void processDetails(Bill bill);

	/**
	 * Calcula el resultado de la factura a partir de los detalles que la componen.
	 * 
	 * @param bill
	 */
	void processResults(Bill bill);

	/**
	 * Acepta los resultados de la factura.
	 * 
	 * @param bill
	 */
	void confirmBill(Bill bill);

	/**
	 * Cancela una factura
	 * 
	 * @param bill
	 */
	void cancelBill(Bill bill);

	/**
	 * Genera la factura rectificada de otra.
	 * 
	 * @param bill
	 * @return
	 */
	Bill rectifyBill(Bill bill);

}
