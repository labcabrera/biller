package com.luckia.biller.core.services.bills.impl;

import com.luckia.biller.core.model.Bill;
import com.luckia.biller.core.model.Store;
import com.luckia.biller.core.services.bills.CodeGenerator;

/**
 * {@link CodeGenerator} de la entidad {@link Bill}
 * 
 * @see AbstractCodeGenerator
 */
public class BillCodeGenerator extends AbstractCodeGenerator<Bill> {

	/**
	 * En el caso de las facturas se genera el numero de factura a partir de la secuencia
	 * de cada establecimiento.
	 * 
	 * @param bill
	 */
	@Override
	public void generateCode(Bill bill) {
		Store store = bill.getSender(Store.class);
		String template = store.getBillSequencePrefix() != null
				? store.getBillSequencePrefix() : "";
		String code = generateCode(template);
		bill.setCode(code);
	}
}
