package com.luckia.biller.core.model;

import java.io.Serializable;
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

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "B_BILL_RAW_DATA")
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuppressWarnings("serial")
public class BillRawData implements Serializable {

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
}
