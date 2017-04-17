package com.luckia.biller.core.model;

import java.io.Serializable;
import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import lombok.Data;

/**
 * Entidad que agrupa los resultados de una liquidacion.
 */
@Embeddable
@Data
@SuppressWarnings("serial")
public class LiquidationResults implements Mergeable<LiquidationResults>, Serializable {

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
	 * Suma de los ajustes manuales no incluidos en la liquidacion de todos los
	 * establecimientos.
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
	 * Resultado efectivo de la liquidacion. Este valor es el resultado de la liquidacion
	 * al que se le suman los ajustes manuales no incluidos en el importe de liquidacion.
	 */
	@Column(name = "LIQUIDATION_EFFECTIVE_AMOUNT", precision = 18, scale = 2)
	private BigDecimal effectiveLiquidationAmount;

	@Override
	public void merge(LiquidationResults entity) {
		if (entity != null) {
			this.liquidationManualInnerAmount = entity.liquidationManualInnerAmount;
			this.cashStoreAmount = entity.cashStoreAmount;
			this.receiverAmount = entity.receiverAmount;
			this.netAmount = entity.netAmount;
			this.vatAmount = entity.vatAmount;
			this.totalAmount = entity.totalAmount;
			this.storeManualOuterAmount = entity.storeManualOuterAmount;
			this.effectiveLiquidationAmount = entity.effectiveLiquidationAmount;
		}
	}
}
