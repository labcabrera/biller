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
import com.luckia.biller.core.model.CostCenter;
import com.luckia.biller.core.services.AuditService;

public class CostCenterFeeder implements Feeder<CostCenter> {

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
				CostCenter center = new CostCenter();
				center.setCode(dataSet.getString("CODE"));
				center.setName(dataSet.getString("NAME"));
				auditService.processCreated(center);
				entityManager.persist(center);
			}
			entityManager.flush();
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

}
