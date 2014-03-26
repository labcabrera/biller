package com.luckia.biller.core.model;

import java.math.BigDecimal;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.luckia.biller.core.serialization.NotSerializable;

@Entity
@Table(name = "B_RAPPEL")
public class Rappel {

	@Id
	@GeneratedValue
	private Long id;

	@ManyToOne(cascade = CascadeType.DETACH, optional = false)
	@JoinColumn(name = "MODEL_ID", referencedColumnName = "ID", nullable = false)
	@NotSerializable
	private BillingModel model;

	@Column(name = "AMOUNT", precision = 18, scale = 2)
	private BigDecimal amount;

	@Column(name = "BONUS_PERCENT", precision = 18, scale = 2)
	private BigDecimal bonusPercent;

	@Column(name = "BONUS_AMOUNT", precision = 18, scale = 2)
	private BigDecimal bonusAmount;

	public Rappel() {
	}

	public Rappel(BigDecimal amount, BigDecimal bonusPercent, BigDecimal bonusAmount) {
		this.amount = amount;
		this.bonusPercent = bonusPercent;
		this.bonusAmount = bonusAmount;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public BillingModel getModel() {
		return model;
	}

	public void setModel(BillingModel model) {
		this.model = model;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	public BigDecimal getBonusPercent() {
		return bonusPercent;
	}

	public void setBonusPercent(BigDecimal bonusPercent) {
		this.bonusPercent = bonusPercent;
	}

	public BigDecimal getBonusAmount() {
		return bonusAmount;
	}

	public void setBonusAmount(BigDecimal bonusAmount) {
		this.bonusAmount = bonusAmount;
	}
}
