package com.luckia.biller.core.services.bills;

import java.util.Date;

import org.apache.commons.lang3.Range;

import com.luckia.biller.core.model.Company;
import com.luckia.biller.core.model.Liquidation;
import com.luckia.biller.core.model.LiquidationDetail;

public interface LiquidationProcessor {

	Liquidation processBills(Company company, Range<Date> range);

	/**
	 * Actualiza el estado de la liquidación y genera el PDF asociado.
	 * 
	 * @param liquidation
	 *            Liquidacion
	 */
	void confirm(Liquidation liquidation);

	/**
	 * Actualiza el resultado de la liquidación (por ejemplo en el caso en el que hayamos modificado una factura o añadido un ajuste
	 * operativo).
	 * 
	 * @param liquidation
	 * @return
	 */
	Liquidation updateResults(Liquidation liquidation);

	Liquidation mergeDetail(LiquidationDetail detail);

	Liquidation removeDetail(LiquidationDetail detail);

}