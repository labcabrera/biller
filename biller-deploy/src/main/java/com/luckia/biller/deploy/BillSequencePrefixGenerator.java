package com.luckia.biller.deploy;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Guice;
import com.luckia.biller.core.LuckiaCoreModule;
import com.luckia.biller.core.model.Store;

/**
 * Clase que actualiza los prefijos de las secuencias de códigos de facturas de los establecimientos.
 */
public class BillSequencePrefixGenerator implements Runnable {

	private static final Logger LOG = LoggerFactory.getLogger(BillSequencePrefixGenerator.class);

	public static void main(String[] args) {
		Guice.createInjector(new LuckiaCoreModule()).getInstance(BillSequencePrefixGenerator.class).run();
	}

	@Inject
	private Provider<EntityManager> entityManagerProvider;

	@Override
	public void run() {
		EntityManager entityManager = entityManagerProvider.get();
		TypedQuery<Store> query = entityManager.createQuery("select s from Store s where s.billSequencePrefix is null order by s.name", Store.class);
		List<Store> stores = query.getResultList();
		LOG.debug("Updating {} store prefix", stores.size());
		entityManager.getTransaction().begin();
		for (Store store : stores) {
			String storeId = StringUtils.leftPad(String.valueOf(store.getId()), 4, '0');
			String billSequencePrefix = String.format("A{year}/%s/{sequence,4}", storeId);
			store.setBillSequencePrefix(billSequencePrefix);
			entityManager.merge(store);
		}
		entityManager.getTransaction().commit();
	}

}
