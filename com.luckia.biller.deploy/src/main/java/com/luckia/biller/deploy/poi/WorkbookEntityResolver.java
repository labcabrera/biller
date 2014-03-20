package com.luckia.biller.deploy.poi;

import java.io.IOException;
import java.util.List;
import java.util.Properties;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.luckia.biller.core.jpa.EntityManagerProvider;
import com.luckia.biller.core.model.Province;
import com.luckia.biller.core.model.Region;
import com.luckia.biller.core.model.StoreType;

public class WorkbookEntityResolver {

	private static final Logger LOG = LoggerFactory.getLogger(WorkbookEntityResolver.class);

	@Inject
	private EntityManagerProvider entityManagerProvider;
	private Properties aliases;

	public WorkbookEntityResolver() throws IOException {
		aliases = new Properties();
		aliases.load(getClass().getResourceAsStream("/region-mapping.properties"));
	}

	public Province resolveProvince(final String name) {
		String aliasKey = getKey("p", name);
		String alias = aliases.containsKey(aliasKey) ? aliases.getProperty(aliasKey) : name;
		EntityManager entityManager = entityManagerProvider.get();
		TypedQuery<Province> query = entityManager.createQuery("select p from Province p where p.name = :name", Province.class);
		List<Province> results = query.setParameter("name", alias).getResultList();
		if (!results.isEmpty()) {
			return results.iterator().next();
		} else {
			LOG.debug("No se encuentra la provincia con nombre '{}'", name);
			return null;
		}
	}

	public Region resolveRegion(String name) {
		String aliasKey = getKey("r", name);
		String alias = aliases.containsKey(aliasKey) ? aliases.getProperty(aliasKey) : name;
		EntityManager entityManager = entityManagerProvider.get();
		TypedQuery<Region> query = entityManager.createQuery("select r from Region r where r.name = :name", Region.class);
		List<Region> results = query.setParameter("name", alias).getResultList();
		if (!results.isEmpty()) {
			return results.iterator().next();
		} else {
			LOG.debug("No se encuentra la localidad con nombre '{}'", name);
			return null;
		}
	}

	public StoreType resolveStoreType(String type) {
		if ("Bar".equals(type)) {
			return StoreType.Bar;
		} else if ("Salón".equals(type)) {
			return StoreType.SalonCorner;
		} else if ("Bingo".equals(type)) {
			return StoreType.BingoCorner;
		} else {
			return null;
		}
	}

	private String getKey(String prefix, String name) {
		return String.format("%s.%s", prefix, name.replaceAll(" ", "_"));

	}

}
