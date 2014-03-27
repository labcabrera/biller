package com.luckia.biller.core.services.bills;

import java.math.BigDecimal;

import com.luckia.biller.core.model.Bill;

/**
 * Servicio encargado de obtener la tasa de juego asociado a una factura. En teoria este valor dependera de la comunidad autonoma, aunque de
 * momento aplicamos el 10% para todas las facturas.
 */
public class BillFeesService {

	// TODO
	public BigDecimal getGameFeesPercent(Bill bill) {
		return new BigDecimal("10.00");
	}
}
