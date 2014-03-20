package com.luckia.biller.core.services.bills;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;

import com.luckia.biller.core.jpa.Sequencer;
import com.luckia.biller.core.model.Bill;

/**
 * Servicio encargado de generar los codigos de las facturas. Estos codigos han de ser consecutivos.
 */
public class BillCodeGenerator {

	@Inject
	private Sequencer sequencer;

	/**
	 * En el caso de las facturas se genera el numero de factura a partir de la secuencia de cada establecimiento.
	 * 
	 * @param bill
	 */
	public void generateCode(Bill bill) {
		Long sequence = sequencer.nextSequence("BILL_CODE");
		String code = StringUtils.leftPad(String.valueOf(sequence), 6, "0");
		bill.setCode(code);
	}
}
