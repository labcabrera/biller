package com.luckia.biller.core.services.bills;

import java.util.Date;

import org.apache.commons.lang3.Range;

import com.luckia.biller.core.model.Company;
import com.luckia.biller.core.model.Liquidation;
import com.luckia.biller.core.model.LiquidationDetail;

/**
 * Componente encargado de realizar las tareas de generacion, confirmacion, eliminacion y
 * actualizacion de liquidaciones.
 */
public interface LiquidationProcessor {

	Liquidation processBills(Company company, Range<Date> range);

	void processResults(Liquidation liquidation);

	/**
	 * Actualiza el estado de la liquidación y genera el PDF asociado.
	 * 
	 * @param liquidation Liquidacion
	 */
	void confirm(Liquidation liquidation);

	/**
	 * Actualiza el resultado de la liquidación (por ejemplo en el caso en el que hayamos
	 * modificado una factura o añadido un ajuste operativo).
	 * 
	 * @param liquidation
	 * @return
	 */
	Liquidation updateLiquidationResults(Liquidation liquidation);

	Liquidation mergeDetail(LiquidationDetail detail);

	Liquidation removeDetail(LiquidationDetail detail);

	/** Borrado fisico de la liquidacion */
	void remove(Liquidation liquidation);

	Liquidation recalculate(String liquidationId);

}