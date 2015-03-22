/*******************************************************************************
 * Copyright (c) 2004, 2012 Kotasoft S.L.
 * All rights reserved. This program and the accompanying materials
 * may only be used prior written consent of Kotasoft S.L.
 ******************************************************************************/
package com.luckia.biller.core.model.common;

import java.io.Serializable;

/**
 * Entidad que representa los parámetros de búsqueda desde el front:
 * <ul>
 * <li>Página actual de resultados (paginación)</li>
 * <li>Número de resultados por página</li>
 * <li>Expresión FIQL con las condiciones de búsqueda</li>
 * </ul>
 * Generalmente las búsquedas devolverán un objeto de tipo {@link SearchResults} con los resultados.
 */
@SuppressWarnings("serial")
public class SearchParams implements Serializable {

	private Integer currentPage;
	private Integer itemsPerPage;
	private String queryString;

	/**
	 * Constructor sin parámetros
	 */
	public SearchParams() {
	}

	/**
	 * Constructor a partir de la expresión de búsqueda, página actual y número de resultados por página
	 * 
	 * @param currentPage
	 * @param itemsPerPage
	 * @param queryString
	 */
	public SearchParams(Integer currentPage, Integer itemsPerPage, String queryString) {
		this.currentPage = currentPage;
		this.itemsPerPage = itemsPerPage;
		this.queryString = queryString;
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
