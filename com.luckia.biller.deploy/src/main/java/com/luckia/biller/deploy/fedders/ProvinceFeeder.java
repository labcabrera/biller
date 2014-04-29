/*******************************************************************************
 * Copyright (c) 2004, 2012 Kotasoft S.L.
 * All rights reserved. This program and the accompanying materials
 * may only be used prior written consent of Kotasoft S.L.
 ******************************************************************************/
package com.luckia.biller.deploy.fedders;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Iterator;

import javax.inject.Inject;
import javax.persistence.EntityManager;

import org.apache.commons.io.Charsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.luckia.biller.core.jpa.EntityManagerProvider;
import com.luckia.biller.core.model.Province;

public class ProvinceFeeder implements Feeder<Province> {

	private static final Logger LOG = LoggerFactory.getLogger(ProvinceFeeder.class);

	@Inject
	private EntityManagerProvider entityManagerProvider;

	@Override
	public void loadEntities(InputStream source) {
		Long t0 = System.currentTimeMillis();
		Long count = 0L;
		Reader reader = new InputStreamReader(source, Charsets.UTF_8);
		JsonParser parser = new JsonParser();
		JsonArray json = parser.parse(reader).getAsJsonArray();
		EntityManager entityManager = entityManagerProvider.get();
		for (Iterator<JsonElement> iterator = json.iterator(); iterator.hasNext();) {
			JsonObject element = iterator.next().getAsJsonObject();
			Province province = new Province();
			province.setId(element.get("id").getAsString());
			province.setCode(element.get("code").getAsString());
			province.setName(element.get("name").getAsString());
			entityManager.persist(province);
			count++;
		}
		entityManager.flush();
		LOG.info("Cargadas {} provincias en {} ms", count, (System.currentTimeMillis() - t0));
	}
}
