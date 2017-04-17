package com.luckia.biller.core.services.bills.recalculation;

import java.io.Serializable;
import java.util.Date;

import lombok.Data;

@Data
@SuppressWarnings("serial")
public class BillRecalculationDetail implements Serializable {

	private String billId;

	private Long storeId;
	private String storeName;

	private Long companyId;
	private String companyName;

	private Date dateFrom;
	private Date dateTo;

}
