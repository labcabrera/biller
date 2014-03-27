package com.luckia.biller.core.services.bills.impl;

import com.luckia.biller.core.model.Company;
import com.luckia.biller.core.model.Liquidation;
import com.luckia.biller.core.services.bills.CodeGenerator;

/**
 * {@link CodeGenerator} de la entidad {@link Liquidation}
 * 
 * @see AbstractCodeGenerator
 */
public class LiquidationCodeGenerator extends AbstractCodeGenerator<Liquidation> {

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.luckia.biller.core.services.bills.CodeGenerator#generateCode(java.lang.Object)
	 */
	@Override
	public void generateCode(Liquidation entity) {
		Company company = entity.getSender(Company.class);
		String template = company.getLiquidationSequencePrefix() != null ? company.getLiquidationSequencePrefix() : "";
		String code = generateCode(template);
		entity.setCode(code);
	}
}
