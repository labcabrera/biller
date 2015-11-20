package com.luckia.biller.deploy.fedders;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.persistence.EntityManager;

import org.apache.commons.io.Charsets;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.luckia.biller.core.model.Province;
import com.luckia.biller.core.model.Region;

public class RegionFeeder implements Feeder<Region> {

	private static final Logger LOG = LoggerFactory.getLogger(RegionFeeder.class);

	@Inject
	private Provider<EntityManager> entityManagerProvider;

	@Override
	public void loadEntities(InputStream source) {
		EntityManager entityManager = entityManagerProvider.get();
		Map<String, Province> provinces = new HashMap<String, Province>();
		for (Province province : entityManager.createQuery("select p from Province p", Province.class).getResultList()) {
			provinces.put(province.getId(), province);
		}
		Reader reader = null;
		long t0 = System.currentTimeMillis();
		try {
			int count = 0;
			reader = new InputStreamReader(source, Charsets.UTF_8);
			BufferedReader bufferedReader = new BufferedReader(reader);
			String line;
			while ((line = bufferedReader.readLine()) != null) {
				String[] split = line.split(";");
				String provinceId = split[0];
				String regionId = split[1];
				String name = split[3];
				Region region = new Region();
				Province province = provinces.get(provinceId);
				Validate.notNull(province, "Missing province " + provinceId);
				region.setId(provinceId + regionId);
				region.setCode(regionId);
				region.setName(name);
				region.setProvince(province);
				entityManager.persist(region);
				if (count++ % 250 == 0) {
					entityManager.flush();
				}
			}
			LOG.info("Cargadas {} regiones en {} ms", count, (System.currentTimeMillis() - t0));
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException ignore) {
					LOG.error(ignore.getMessage());
				}
			}
		}
	}

}
