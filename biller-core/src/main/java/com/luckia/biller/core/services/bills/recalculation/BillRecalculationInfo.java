package com.luckia.biller.core.services.bills.recalculation;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import com.luckia.biller.core.model.Company;
import com.luckia.biller.core.model.Store;

@SuppressWarnings("serial")
public class BillRecalculationInfo implements Serializable {

	private Date from;
	private Date to;
	private Store store;
	private Company company;
	private List<BillRecalculationDetail> currentBills;
	private List<BillRecalculationDetail> nonExistingBills;

	public Date getFrom() {
		return from;
	}

	public void setFrom(Date from) {
		this.from = from;
	}

	public Date getTo() {
		return to;
	}

	public void setTo(Date to) {
		this.to = to;
	}

	public Store getStore() {
		return store;
	}

	public void setStore(Store store) {
		this.store = store;
	}

	public Company getCompany() {
		return company;
	}

	public void setCompany(Company company) {
		this.company = company;
	}

	public List<BillRecalculationDetail> getCurrentBills() {
		return currentBills;
	}

	public void setCurrentBills(List<BillRecalculationDetail> currentBills) {
		this.currentBills = currentBills;
	}

	public List<BillRecalculationDetail> getNonExistingBills() {
		return nonExistingBills;
	}

	public void setNonExistingBills(List<BillRecalculationDetail> nonExistingBills) {
		this.nonExistingBills = nonExistingBills;
	}
}
