package com.luckia.biller.core.model;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import com.luckia.biller.core.jpa.Mergeable;

@Embeddable
public class BillingModelAttributes implements Mergeable<BillingModelAttributes> {

	/**
	 * Porcentaje sobre el importe apostado.
	 */
	@Column(name = "STAKES_PERCENT", precision = 6, scale = 2)
	private BigDecimal stakesPercent;

	/**
	 * Porcentaje sobre los ingresos brutos de juego: {importe apostado - cancelaciones - premios}
	 */
	@Column(name = "GGR_PERCENT", precision = 6, scale = 2)
	private BigDecimal ggrPercent;

	/**
	 * Porcentaje sobre los ingresos netos de juego: {ggr - tasa de juego}
	 */
	@Column(name = "NGR_PERCENT", precision = 6, scale = 2)
	private BigDecimal ngrPercent;

	/**
	 * Resultado neto: {ngr - gastos de co-explotacion}<br>
	 * Este valor solo tiene sentido para las liquidaciones ya que en las facturas nunca habrá co-explotación.
	 */
	@Column(name = "NR_PERCENT", precision = 6, scale = 2)
	private BigDecimal nrPercent;

	/**
	 * Gastos mensuales de co-explotacionl. Este valor se utiliza como un importe fijo a la hora de calcular el NR (recordar que el NR es el
	 * resultado de restar al NGR los gastos operativos).<br>
	 * A diferencia de los gastos comerciales y gastos del servicio de atencion al cliente este valor no se añade como concepto en las
	 * facturas/liquidaciones.
	 */
	@Column(name = "CO_OP_MONTLY_FEES", precision = 18, scale = 2)
	private BigDecimal coOperatingMonthlyFees;

	/**
	 * Gastos mensuales de atencion comercial (importe fijo en euros)
	 */
	@Column(name = "COMMERCIAL_MONTLY_FEES", precision = 18, scale = 2)
	private BigDecimal commercialMonthlyFees;

	/**
	 * Gastos mensuales SAT (servicio de atención al cliente)
	 */
	@Column(name = "SAT_MONTLY_FEES", precision = 18, scale = 2)
	private BigDecimal satMonthlyFees;

	@Column(name = "PRICE_PER_LOCATION", precision = 18, scale = 2)
	private BigDecimal pricePerLocation;

	public BigDecimal getStakesPercent() {
		return stakesPercent;
	}

	public void setStakesPercent(BigDecimal stakesPercent) {
		this.stakesPercent = stakesPercent;
	}

	public BigDecimal getGgrPercent() {
		return ggrPercent;
	}

	public void setGgrPercent(BigDecimal ggrPercent) {
		this.ggrPercent = ggrPercent;
	}

	public BigDecimal getNgrPercent() {
		return ngrPercent;
	}

	public void setNgrPercent(BigDecimal ngrPercent) {
		this.ngrPercent = ngrPercent;
	}

	public BigDecimal getNrPercent() {
		return nrPercent;
	}

	public void setNrPercent(BigDecimal nrPercent) {
		this.nrPercent = nrPercent;
	}

	public BigDecimal getCoOperatingMonthlyFees() {
		return coOperatingMonthlyFees;
	}

	public void setCoOperatingMonthlyFees(BigDecimal coOperatingMonthlyFees) {
		this.coOperatingMonthlyFees = coOperatingMonthlyFees;
	}

	public BigDecimal getCommercialMonthlyFees() {
		return commercialMonthlyFees;
	}

	public void setCommercialMonthlyFees(BigDecimal commercialMonthlyFees) {
		this.commercialMonthlyFees = commercialMonthlyFees;
	}

	public BigDecimal getSatMonthlyFees() {
		return satMonthlyFees;
	}

	public void setSatMonthlyFees(BigDecimal satMonthlyFees) {
		this.satMonthlyFees = satMonthlyFees;
	}

	public BigDecimal getPricePerLocation() {
		return pricePerLocation;
	}

	public void setPricePerLocation(BigDecimal pricePerLocation) {
		this.pricePerLocation = pricePerLocation;
	}

	@Override
	public void merge(BillingModelAttributes entity) {
		if (entity != null) {
			this.commercialMonthlyFees = entity.commercialMonthlyFees;
			this.coOperatingMonthlyFees = entity.coOperatingMonthlyFees;
			this.ggrPercent = entity.ggrPercent;
			this.ngrPercent = entity.ngrPercent;
			this.nrPercent = entity.nrPercent;
			this.satMonthlyFees = entity.satMonthlyFees;
			this.stakesPercent = entity.stakesPercent;
			this.pricePerLocation = entity.pricePerLocation;
		}
	}
}
