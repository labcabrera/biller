package com.luckia.biller.core.model;

public enum BillType {

	Common("Ordinaria"),

	Rectified("Rectificativa");

	private String desc;

	private BillType(String desc) {
		this.desc = desc;
	}

	public String desc() {
		return desc;
	}
}
