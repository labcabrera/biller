package com.luckia.biller.core.services.bills;

import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.Range;

import com.luckia.biller.core.model.Company;
import com.luckia.biller.core.model.Liquidation;

public interface LiquidationProcessor {

	public abstract List<Liquidation> process(Company company, Range<Date> range);

}