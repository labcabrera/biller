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

	/** Suma de todos los ajustes operativos de las facturas */
	@Column(name = "ADJUSTMENT_AMOUNT", precision = 18, scale = 2)
	private BigDecimal adjustmentAmount;

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

	/**
	 * Suma de los bases imponibles de todas las mini-liquidaciones.
	 */
	@Column(name = "NET_AMOUNT", precision = 18, scale = 2)
	private BigDecimal netAmount;

	@Column(name = "VAT_AMOUNT", precision = 18, scale = 2)
	private BigDecimal vatAmount;

	@Column(name = "TOTAL_AMOUNT", precision = 18, scale = 2)
	private BigDecimal totalAmount;

	/**
	 * Suma de los ajustes manuales no incluidos en la liquidacion de todos los establecimientos.
	 */
	@Column(name = "STORE_MANUAL_OUTER_AMOUNT", precision = 18, scale = 2)
	private BigDecimal storeManualOuterAmount;

	/**
	 * Importe total de ajustes manuales no incluidos en el importe de liquidacion.
	 */
	@Column(name = "TOTAL_OUTER_AMOUNT", precision = 18, scale = 2)
	private BigDecimal totalOuterAmount;

	@Column(name = "LIQUIDATION_EFFECTIVE_AMOUNT", precision = 18, scale = 2)
	private BigDecimal effectiveLiquidationAmount;

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

	public BigDecimal getTotalAmount() {
		return totalAmount;
	}

	public void setTotalAmount(BigDecimal totalAmount) {
		this.totalAmount = totalAmount;
	}

	public BigDecimal getStoreManualOuterAmount() {
		return storeManualOuterAmount;
	}

	public void setStoreManualOuterAmount(BigDecimal storeManualOuterAmount) {
		this.storeManualOuterAmount = storeManualOuterAmount;
	}

	public BigDecimal getTotalOuterAmount() {
		return totalOuterAmount;
	}

	public void setTotalOuterAmount(BigDecimal totalOuterAmount) {
		this.totalOuterAmount = totalOuterAmount;
	}

	public BigDecimal getEffectiveLiquidationAmount() {
		return effectiveLiquidationAmount;
	}

	public void setEffectiveLiquidationAmount(BigDecimal effectiveLiquidationAmount) {
		this.effectiveLiquidationAmount = effectiveLiquidationAmount;
	}

	@Override
	public void merge(LiquidationResults entity) {
		if (entity != null) {
			this.adjustmentAmount = entity.adjustmentAmount;
			this.betAmount = entity.betAmount;
			this.cashStoreAmount = entity.cashStoreAmount;
			this.receiverAmount = entity.receiverAmount;
			this.satAmount = entity.satAmount;
			this.senderAmount = entity.senderAmount;
			this.storeAmount = entity.storeAmount;
			this.netAmount = entity.netAmount;
			this.vatAmount = entity.vatAmount;
			this.totalAmount = entity.totalAmount;
			this.storeManualOuterAmount = entity.storeManualOuterAmount;
			this.effectiveLiquidationAmount = entity.effectiveLiquidationAmount;
		}
	}

}
