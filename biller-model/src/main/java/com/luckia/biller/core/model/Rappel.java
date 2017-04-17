package com.luckia.biller.core.model;

import java.io.Serializable;
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

import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "B_RAPPEL")
@Data
@NoArgsConstructor
@SuppressWarnings("serial")
public class Rappel implements Mergeable<Rappel>, Serializable {

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

	public Rappel(BigDecimal amount, BigDecimal bonusPercent, BigDecimal bonusAmount) {
		this.amount = amount;
		this.bonusPercent = bonusPercent;
		this.bonusAmount = bonusAmount;
	}

	@Override
	public void merge(Rappel entity) {
		if (entity != null) {
			this.amount = entity.amount;
			this.bonusAmount = entity.bonusAmount;
		}
	}

	@Override
	public String toString() {
		return "Rappel [amount=" + amount + ", bonusAmount=" + bonusAmount + "]";
	}

}
