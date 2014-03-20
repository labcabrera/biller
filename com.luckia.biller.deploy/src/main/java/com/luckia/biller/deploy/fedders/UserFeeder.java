/*******************************************************************************
 * Copyright (c) 2004, 2012 Kotasoft S.L.
 * All rights reserved. This program and the accompanying materials
 * may only be used prior written consent of Kotasoft S.L.
 ******************************************************************************/
package com.luckia.biller.deploy.fedders;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Calendar;
import java.util.Date;

import javax.inject.Inject;
import javax.persistence.EntityManager;

import net.sf.flatpack.DataSet;
import net.sf.flatpack.DefaultParserFactory;
import net.sf.flatpack.Parser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.luckia.biller.core.jpa.EntityManagerProvider;
import com.luckia.biller.core.model.User;

public class UserFeeder implements Feeder<User> {

	private static final Logger LOG = LoggerFactory.getLogger(UserFeeder.class);

	@Inject
	private EntityManagerProvider entityManagerProvider;

	@Override
	public void loadEntities(InputStream source) {
		try {
			Reader reader = new InputStreamReader(source, "UTF8");
			Parser parser = DefaultParserFactory.getInstance().newDelimitedParser(reader, ',', '"');
			DataSet dataSet = parser.parse();
			Date now = Calendar.getInstance().getTime();
			EntityManager entityManager = entityManagerProvider.get();
			Long t0 = System.currentTimeMillis();
			Long count = 0L;
			while (dataSet.next()) {
				User user = new User();
				user.setName(dataSet.getString("NAME"));
				user.setEmail(dataSet.getString("EMAIL"));
				user.setPasswordDigest(dataSet.getString("PASSWORD_DIGEST"));
				user.setCreated(now);
				entityManager.persist(user);
				count++;
			}
			LOG.info("Cargadas {} regiones en {} ms", count, (System.currentTimeMillis() - t0));
		} catch (Exception ex) {
			throw new RuntimeException(ex);

		}
	}
}
