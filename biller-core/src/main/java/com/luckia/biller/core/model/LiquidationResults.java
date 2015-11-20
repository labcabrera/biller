package com.luckia.biller.core.model;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import com.luckia.biller.core.jpa.Mergeable;

/**
 * Entidad que agrupa los resultados de una liquidacion.
 */
@Embeddable
public class LiquidationResults implements Mergeable<LiquidationResults> {

	/**
	 * Suma de todos los saldos de caja de los establecimientos.
	 */
	@Column(name = "CASH_STORE_AMOUNT", precision = 18, scale = 2)
	private BigDecimal cashStoreAmount;

	/**
	 * Saldo de caja ajustado despues de los ajustes manuales.
	 */
	@Column(name = "CASH_STORE_EFFECTIVE_AMOUNT", precision = 18, scale = 2)
	private BigDecimal cashStoreEffectiveAmount;

	/**
	 * Suma de los bases imponibles de todas las mini-liquidaciones.
	 */
	@Column(name = "NET_AMOUNT", precision = 18, scale = 2)
	private BigDecimal netAmount;

	@Column(name = "VAT_AMOUNT", precision = 18, scale = 2)
	private BigDecimal vatAmount;

	@Column(name = "TOTAL_AMOUNT", precision = 18, scale = 2)
	private BigDecimal totalAmount;

	@Column(name = "LIQUIDATION_MANUAL_INNER_AMOUNT", precision = 18, scale = 2)
	private BigDecimal liquidationManualInnerAmount;

	/**
	 * Suma de los ajustes manuales no incluidos en la liquidacion de todos los establecimientos.
	 */
	@Column(name = "STORE_MANUAL_OUTER_AMOUNT", precision = 18, scale = 2)
	private BigDecimal storeManualOuterAmount;

	/**
	 * Importe total de ajustes manuales no incluidos en el importe de liquidacion.
	 */
	@Column(name = "LIQUIDATION_MANUAL_OUTER_AMOUNT", precision = 18, scale = 2)
	private BigDecimal liquidationManualOuterAmount;

	/** Resultado de la liquidacion de Egasa */
	@Column(name = "RECEIVER_AMOUNT", precision = 18, scale = 2)
	private BigDecimal receiverAmount;

	/**
	 * Resultado efectivo de la liquidacion. Este valor es el resultado de la liquidacion al que se le suman los ajustes manuales no incluidos en el importe de liquidacion.
	 */
	@Column(name = "LIQUIDATION_EFFECTIVE_AMOUNT", precision = 18, scale = 2)
	private BigDecimal effectiveLiquidationAmount;

	/**
	 * Version con la que se ha generado la liquidacion (utilizado para migraciones)
	 */
	@Column(name = "MODEL_VERSION", length = 8)
	private String modelVersion;

	// obsoletes (migrar) -----------------------------------------------------------------

	// /** Suma de todos los conceptos por apuestas de las facturas */
	// @Column(name = "BET_AMOUNT", precision = 18, scale = 2)
	// private BigDecimal betAmount;
	//
	// /** Suma de todos los conceptos de facturas cuando en el modelo se incluyen en la liquidacion */
	// @Column(name = "STORE_AMOUNT", precision = 18, scale = 2)
	// private BigDecimal storeAmount;
	//
	// /** Suma de todos los conceptos de servicio de atención al cliente de las facturas */
	// @Column(name = "SAT_AMOUNT", precision = 18, scale = 2)
	// private BigDecimal satAmount;
	//
	// @Column(name = "PRICE_PER_LOCATION_AMOUNT", precision = 18, scale = 2)
	// private BigDecimal pricePerLocation;
	//
	// /** Resultado de la liquidacion del operador */
	// @Column(name = "SENDER_AMOUNT", precision = 18, scale = 2)
	// private BigDecimal senderAmount;

	// --------------------------------------------------------------------------------------------------------------------

	// public BigDecimal getBetAmount() {
	// return betAmount;
	// }
	//
	// public void setBetAmount(BigDecimal betAmount) {
	// this.betAmount = betAmount;
	// }
	//
	// public BigDecimal getSatAmount() {
	// return satAmount;
	// }
	//
	// public void setSatAmount(BigDecimal satAmount) {
	// this.satAmount = satAmount;
	// }
	//
	// public BigDecimal getStoreAmount() {
	// return storeAmount;
	// }
	//
	// public void setStoreAmount(BigDecimal value) {
	// this.storeAmount = value;
	// }

	public BigDecimal getLiquidationManualInnerAmount() {
		return liquidationManualInnerAmount;
	}

	public void setLiquidationManualInnerAmount(BigDecimal value) {
		this.liquidationManualInnerAmount = value;
	}

	public BigDecimal getCashStoreAmount() {
		return cashStoreAmount;
	}

	public void setCashStoreAmount(BigDecimal value) {
		this.cashStoreAmount = value;
	}

	public BigDecimal getReceiverAmount() {
		return receiverAmount;
	}

	public void setReceiverAmount(BigDecimal value) {
		this.receiverAmount = value;
	}

	public BigDecimal getCashStoreEffectiveAmount() {
		return cashStoreEffectiveAmount;
	}

	public void setCashStoreEffectiveAmount(BigDecimal value) {
		this.cashStoreEffectiveAmount = value;
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

	public void setTotalAmount(BigDecimal value) {
		this.totalAmount = value;
	}

	public BigDecimal getStoreManualOuterAmount() {
		return storeManualOuterAmount;
	}

	public void setStoreManualOuterAmount(BigDecimal value) {
		this.storeManualOuterAmount = value;
	}

	public BigDecimal getLiquidationManualOuterAmount() {
		return liquidationManualOuterAmount;
	}

	public void setLiquidationManualOuterAmount(BigDecimal value) {
		this.liquidationManualOuterAmount = value;
	}

	public BigDecimal getEffectiveLiquidationAmount() {
		return effectiveLiquidationAmount;
	}

	public void setEffectiveLiquidationAmount(BigDecimal value) {
		this.effectiveLiquidationAmount = value;
	}

	public String getModelVersion() {
		return modelVersion;
	}

	public void setModelVersion(String modelVersion) {
		this.modelVersion = modelVersion;
	}

	@Override
	public void merge(LiquidationResults entity) {
		if (entity != null) {
			this.liquidationManualInnerAmount = entity.liquidationManualInnerAmount;
			// this.betAmount = entity.betAmount;
			this.cashStoreAmount = entity.cashStoreAmount;
			this.receiverAmount = entity.receiverAmount;
			// this.satAmount = entity.satAmount;
			// this.senderAmount = entity.senderAmount;
			// this.storeAmount = entity.storeAmount;
			this.netAmount = entity.netAmount;
			this.vatAmount = entity.vatAmount;
			this.totalAmount = entity.totalAmount;
			this.storeManualOuterAmount = entity.storeManualOuterAmount;
			this.effectiveLiquidationAmount = entity.effectiveLiquidationAmount;
			this.modelVersion = entity.modelVersion;
		}
	}

}
