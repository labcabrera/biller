package com.luckia.biller.core.services.bills;

import java.util.Date;

import org.apache.commons.lang3.Range;

import com.luckia.biller.core.model.Store;

public interface RappelStoreProcessor {

	void processRappel(Store store, Range<Date> range);

}
