package com.luckia.biller.core.model;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.luckia.biller.core.serialization.NotSerializable;

@Entity
@Table(name = "B_BILL_RAW_DATA")
public class BillRawData {

	@Id
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "BILL_ID", nullable = false)
	@NotSerializable
	private Bill bill;

	@Id
	@Column(name = "CONCEPT", nullable = false, length = 45)
	@Enumerated(EnumType.STRING)
	private BillConcept concept;

	@Column(name = "CONCEPT_AMOUNT", nullable = false, precision = 18, scale = 2)
	private BigDecimal amount;

	public BillRawData() {
	}

	public BillRawData(Bill bill, BillConcept concept, BigDecimal amount) {
		this.bill = bill;
		this.concept = concept;
		this.amount = amount;
	}

	public Bill getBill() {
		return bill;
	}

	public void setBill(Bill bill) {
		this.bill = bill;
	}

	public BillConcept getConcept() {
		return concept;
	}

	public void setConcept(BillConcept concept) {
		this.concept = concept;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

}
