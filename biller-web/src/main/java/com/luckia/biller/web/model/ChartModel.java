package com.luckia.biller.web.model;

import java.math.BigDecimal;

/**
 * Entidad utilizada para genenerar las graficas del dashboard.
 */
public class ChartModel {

	private String label;
	private Double value;

	public ChartModel() {
	}

	public ChartModel(Integer label, BigDecimal number) {
		this.label = String.valueOf(label);
		if (number != null) {
			this.value = number.doubleValue();
		}
	}

	public ChartModel(Integer label, Long number) {
		this.label = String.valueOf(label);
		if (number != null) {
			this.value = number.doubleValue();
		}
	}

	public ChartModel(String label, Double number) {
		this.label = label;
		if (number != null) {
			this.value = number;
		}
	}

	public ChartModel(Enum<?> name, Long number) {
		super();
		this.value = Double.valueOf(number);
		if (name != null) {
			this.label = name.getClass().getSimpleName() + "." + name.name();
		}
	}

	public ChartModel(String name, Long number) {
		super();
		this.label = name;
		this.value = Double.valueOf(number);
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public Double getValue() {
		return value;
	}

	public void setValue(Double value) {
		this.value = value;
	}
}
