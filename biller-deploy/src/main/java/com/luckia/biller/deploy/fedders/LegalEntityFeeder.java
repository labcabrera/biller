package com.luckia.biller.deploy.fedders;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

import org.apache.commons.lang3.StringUtils;

import com.luckia.biller.core.common.BillerException;
import com.luckia.biller.core.model.Address;
import com.luckia.biller.core.model.IdCard;
import com.luckia.biller.core.model.IdCardType;
import com.luckia.biller.core.model.LegalEntity;
import com.luckia.biller.core.model.Province;
import com.luckia.biller.core.model.Region;
import com.luckia.biller.core.services.AuditService;

import lombok.extern.slf4j.Slf4j;
import net.sf.flatpack.DataSet;
import net.sf.flatpack.DefaultParserFactory;
import net.sf.flatpack.Parser;

@Slf4j
public abstract class LegalEntityFeeder<T> implements Feeder<T> {

	@Inject
	protected Provider<EntityManager> entityManagerProvider;
	@Inject
	protected AuditService auditService;

	@Override
	public void loadEntities(InputStream source) {
		try {
			Reader reader = new InputStreamReader(source, "UTF8");
			Parser parser = DefaultParserFactory.getInstance().newDelimitedParser(reader,
					',', '"');
			DataSet dataSet = parser.parse();
			EntityManager entityManager = entityManagerProvider.get();
			while (dataSet.next()) {
				LegalEntity entity = buildEntity(dataSet);
				parseCommonData(entity, dataSet);
				auditService.processCreated(entity);
				entityManager.persist(entity);
			}
			entityManager.flush();
		}
		catch (Exception ex) {
			log.error("Feeder error", ex);
			throw new BillerException(ex);
		}
	}

	protected abstract LegalEntity buildEntity(DataSet dataSet);

	protected void parseCommonData(LegalEntity entity, DataSet dataSet) {
		String name = dataSet.getString("NAME");
		String email = dataSet.getString("EMAIL");
		entity.setName(name);
		entity.setEmail(email);
		parseIdCar(entity, dataSet);
		parseLegalEntityAddress(entity, dataSet);
		parseLegalEntityParent(entity, dataSet);
	}

	protected void parseIdCar(LegalEntity entity, DataSet dataSet) {
		if (dataSet.contains("CIF")) {
			entity.setIdCard(new IdCard(IdCardType.CIF, dataSet.getString("CIF")));
		}
		else {
			entity.setIdCard(new IdCard(IdCardType.NIF, dataSet.getString("NIF")));
		}
	}

	protected void parseLegalEntityAddress(LegalEntity entity, DataSet dataSet) {
		EntityManager entityManager = entityManagerProvider.get();
		String regionId = dataSet.getString("REGION_ID");
		String provinceId = dataSet.getString("PROVINCE_ID");
		String road = dataSet.getString("ROAD");
		entity.setAddress(new Address());
		entity.getAddress().setProvince(entityManager.find(Province.class, provinceId));
		entity.getAddress()
				.setRegion(entityManager.find(Region.class, provinceId + regionId));
		entity.getAddress().setRoad(road);
	}

	protected void parseLegalEntityParent(LegalEntity entity, DataSet dataSet) {
		if (dataSet.contains("PARENT")) {
			String parent = dataSet.getString("PARENT");
			if (StringUtils.isNotBlank(parent)) {
				try {
					EntityManager entityManager = entityManagerProvider.get();
					TypedQuery<LegalEntity> query = entityManager.createQuery(
							"select i from LegalEntity i where i.idCard.number = :cif",
							LegalEntity.class);
					LegalEntity legalEntityParent = query.setParameter("cif", parent)
							.getSingleResult();
					entity.setParent(legalEntityParent);
				}
				catch (NoResultException ex) {
					throw new BillerException(
							"No se encuentra la entidad legal con CIF " + parent, ex);
				}
			}
		}
	}
}
