/*******************************************************************************
 * Copyright (c) 2004, 2012 Kotasoft S.L.
 * All rights reserved. This program and the accompanying materials
 * may only be used prior written consent of Kotasoft S.L.
 ******************************************************************************/
package com.luckia.biller.core.model.common;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

@SuppressWarnings("serial")
public class SearchParams implements Serializable {

	private Map<String, Object> params;
	private Integer currentPage;
	private Integer itemsPerPage;
	private String queryString;

	public SearchParams() {
		params = new LinkedHashMap<String, Object>();
	}

	public boolean containsKey(String key) {
		return params != null && params.containsKey(key);
	}

	public Object get(String key) {
		return params != null ? params.get(key) : null;
	}

	@SuppressWarnings("unchecked")
	public <T> T get(String name, Class<T> clazz) {
		return params.containsKey(name) ? (T) params.get(name) : null;
	}

	public Map<String, Object> getParams() {
		return params;
	}

	public void setParams(Map<String, Object> params) {
		this.params = params;
	}

	public Integer getCurrentPage() {
		return currentPage;
	}

	public void setCurrentPage(Integer currentPage) {
		this.currentPage = currentPage;
	}

	public Integer getItemsPerPage() {
		return itemsPerPage;
	}

	public void setItemsPerPage(Integer itemsPerPage) {
		this.itemsPerPage = itemsPerPage;
	}

	public String getQueryString() {
		return queryString;
	}

	public void setQueryString(String queryString) {
		this.queryString = queryString;
	}
}
