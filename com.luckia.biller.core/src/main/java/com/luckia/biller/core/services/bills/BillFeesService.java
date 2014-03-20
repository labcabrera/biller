package com.luckia.biller.core.services.bills;

import java.math.BigDecimal;

import com.luckia.biller.core.model.Bill;

public class BillFeesService {

	/**
	 * Obtiene la tasa de juego asociado a una factura. En teoria este valor dependera de la comunidad autonoma, aunque de momento aplicamos
	 * el 10% para todas las facturas.
	 * 
	 * @param bill
	 * @return
	 */
	// TODO estudiar la parametrizacion por provincia. No tengo claro como se esta obteniendo
	public BigDecimal getGameFeesPercent(Bill bill) {
		return new BigDecimal("10.00");
	}
}
