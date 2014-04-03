package com.luckia.biller.core.services.bills;

import java.math.BigDecimal;
import java.util.Date;

import org.apache.commons.lang3.Range;

import com.luckia.biller.core.model.RappelStoreBonus;
import com.luckia.biller.core.model.Store;

/**
 * Definicion del servicio de calculo anual de rappel de los establecimeintos.
 */
public interface RappelStoreProcessor {

	/**
	 * Calcula el rappel por defecto (sin aplicar prorateo) de un establecimiento para un determinado rango de tiempo.
	 * 
	 * @param store
	 * @param range
	 */
	void processRappel(Store store, Range<Date> range);

	/**
	 * Actualiza el bonus de rappel de un establecimiento a partir de un determinado prorateo.
	 * 
	 * @param rappelStoreBonus
	 * @param prorata
	 */
	void updateRappel(RappelStoreBonus rappelStoreBonus, BigDecimal prorata);

}
