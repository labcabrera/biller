package com.luckia.biller.core.services.pdf;

import java.math.BigDecimal;

import com.google.gson.GsonBuilder;

public class PDFLiquidationDetail {

	private String name;
	private BigDecimal netAmount;
	private BigDecimal vatAmount;
	private BigDecimal amount;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public BigDecimal getNetAmount() {
		return netAmount;
	}

	public void setNetAmount(BigDecimal netAmount) {
		this.netAmount = netAmount;
	}

	public BigDecimal getVatAmount() {
		return vatAmount;
	}

	public void setVatAmount(BigDecimal vatAmount) {
		this.vatAmount = vatAmount;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	public PDFLiquidationDetail init(String name) {
		this.name = name;
		this.netAmount = this.vatAmount = this.amount = BigDecimal.ZERO;
		return this;
	}

	@Override
	public String toString() {
		return new GsonBuilder().create().toJson(this);
	}

}
