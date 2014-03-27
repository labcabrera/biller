/*******************************************************************************
 * Copyright (c) 2004, 2012 Kotasoft S.L.
 * All rights reserved. This program and the accompanying materials
 * may only be used prior written consent of Kotasoft S.L.
 ******************************************************************************/
package com.luckia.biller.deploy.fedders;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import javax.inject.Inject;
import javax.persistence.EntityManager;

import net.sf.flatpack.DataSet;
import net.sf.flatpack.DefaultParserFactory;
import net.sf.flatpack.Parser;

import com.luckia.biller.core.jpa.EntityManagerProvider;
import com.luckia.biller.core.model.CompanyGroup;
import com.luckia.biller.core.services.AuditService;

public class CompanyGroupFeeder implements Feeder<CompanyGroup> {

	@Inject
	private EntityManagerProvider entityManagerProvider;
	@Inject
	private AuditService auditService;

	@Override
	public void loadEntities(InputStream source) {
		try {
			Reader reader = new InputStreamReader(source, "UTF8");
			Parser parser = DefaultParserFactory.getInstance().newDelimitedParser(reader, ',', '"');
			DataSet dataSet = parser.parse();
			EntityManager entityManager = entityManagerProvider.get();
			while (dataSet.next()) {
				CompanyGroup entity = new CompanyGroup();
				entity.setName(dataSet.getString("NAME"));
				auditService.processCreated(entity);
				entityManager.persist(entity);
			}
			entityManager.flush();
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}
}
