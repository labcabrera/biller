package com.luckia.biller.core.model;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import com.luckia.biller.core.jpa.Mergeable;

@Embeddable
public class LiquidationResults implements Mergeable<LiquidationResults> {

	/** Suma de todos los conceptos por apuestas de las facturas */
	@Column(name = "BET_AMOUNT", precision = 18, scale = 2)
	private BigDecimal betAmount;

	/** Suma de todos los conceptos de facturas cuando en el modelo se incluyen en la liquidacion */
	@Column(name = "STORE_AMOUNT", precision = 18, scale = 2)
	private BigDecimal storeAmount;

	/** Suma de todos los conceptos de servicio de atención al cliente de las facturas */
	@Column(name = "SAT_AMOUNT", precision = 18, scale = 2)
	private BigDecimal satAmount;

	@Column(name = "PRICE_PER_LOCATION_AMOUNT", precision = 18, scale = 2)
	private BigDecimal pricePerLocation;

	/** Suma de todos los ajustes manuales de las facturas */
	@Column(name = "OTHER_AMOUNT", precision = 18, scale = 2)
	private BigDecimal otherAmount;

	/** Suma de todos los ajustes operativos de las facturas */
	@Column(name = "ADJUSTMENT_AMOUNT", precision = 18, scale = 2)
	private BigDecimal adjustmentAmount;

	/**
	 * Parte correspondiente al operador de los ajustes operativos. Recordar que los ajustes operativos se reparten entre Egasa y el
	 * operador al 50%
	 */
	@Column(name = "ADJUSTMENT_SHARED_AMOUNT", precision = 18, scale = 2)
	private BigDecimal adjustmentSharedAmount;

	/** Saldo de caja (antes de los ajustes operativos */
	@Column(name = "CASH_STORE_AMOUNT", precision = 18, scale = 2)
	private BigDecimal cashStoreAmount;

	/** Resultado de la liquidacion del operador */
	@Column(name = "SENDER_AMOUNT", precision = 18, scale = 2)
	private BigDecimal senderAmount;

	/** Resultado de la liquidacion de Egasa */
	@Column(name = "RECEIVER_AMOUNT", precision = 18, scale = 2)
	private BigDecimal receiverAmount;

	@Column(name = "CASH_STORE_ADJUSTMENT_AMOUNT", precision = 18, scale = 2)
	private BigDecimal cashStoreAdjustmentAmount;

	public BigDecimal getBetAmount() {
		return betAmount;
	}

	public void setBetAmount(BigDecimal betAmount) {
		this.betAmount = betAmount;
	}

	public BigDecimal getSatAmount() {
		return satAmount;
	}

	public void setSatAmount(BigDecimal satAmount) {
		this.satAmount = satAmount;
	}

	public BigDecimal getStoreAmount() {
		return storeAmount;
	}

	public void setStoreAmount(BigDecimal storeAmount) {
		this.storeAmount = storeAmount;
	}

	public BigDecimal getOtherAmount() {
		return otherAmount;
	}

	public void setOtherAmount(BigDecimal otherAmount) {
		this.otherAmount = otherAmount;
	}

	public BigDecimal getAdjustmentAmount() {
		return adjustmentAmount;
	}

	public void setAdjustmentAmount(BigDecimal adjustmentAmount) {
		this.adjustmentAmount = adjustmentAmount;
	}

	public BigDecimal getCashStoreAmount() {
		return cashStoreAmount;
	}

	public void setCashStoreAmount(BigDecimal cashStoreAmount) {
		this.cashStoreAmount = cashStoreAmount;
	}

	public BigDecimal getSenderAmount() {
		return senderAmount;
	}

	public void setSenderAmount(BigDecimal senderAmount) {
		this.senderAmount = senderAmount;
	}

	public BigDecimal getReceiverAmount() {
		return receiverAmount;
	}

	public void setReceiverAmount(BigDecimal receiverAmount) {
		this.receiverAmount = receiverAmount;
	}

	public BigDecimal getAdjustmentSharedAmount() {
		return adjustmentSharedAmount;
	}

	public void setAdjustmentSharedAmount(BigDecimal adjustmentSharedAmount) {
		this.adjustmentSharedAmount = adjustmentSharedAmount;
	}

	public BigDecimal getCashStoreAdjustmentAmount() {
		return cashStoreAdjustmentAmount;
	}

	public void setCashStoreAdjustmentAmount(BigDecimal cashStoreAdjustmentAmount) {
		this.cashStoreAdjustmentAmount = cashStoreAdjustmentAmount;
	}

	public BigDecimal getPricePerLocation() {
		return pricePerLocation;
	}

	public void setPricePerLocation(BigDecimal pricePerLocation) {
		this.pricePerLocation = pricePerLocation;
	}

	@Override
	public void merge(LiquidationResults entity) {
		if (entity != null) {
			this.adjustmentAmount = entity.adjustmentAmount;
			this.betAmount = entity.betAmount;
			this.cashStoreAmount = entity.cashStoreAmount;
			this.otherAmount = entity.otherAmount;
			this.receiverAmount = entity.receiverAmount;
			this.satAmount = entity.satAmount;
			this.senderAmount = entity.senderAmount;
			this.storeAmount = entity.storeAmount;
		}
	}

}
