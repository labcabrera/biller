package com.luckia.biller.core.services.bills;

import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.Range;

import com.luckia.biller.core.model.Company;
import com.luckia.biller.core.model.Liquidation;
import com.luckia.biller.core.model.LiquidationDetail;

public interface LiquidationProcessor {

	List<Liquidation> processBills(Company company, Range<Date> range);

	/**
	 * Actualiza el estado de la liquidación y genera el PDF asociado.
	 * 
	 * @param liquidation
	 *            Liquidacion
	 */
	void confirm(Liquidation liquidation);

	Liquidation mergeDetail(LiquidationDetail detail);

	Liquidation removeDetail(LiquidationDetail detail);

}