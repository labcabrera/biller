package com.luckia.biller.core.jpa;

import java.util.Arrays;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.junit.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.persist.PersistService;
import com.luckia.biller.core.BillerModule;
import com.luckia.biller.core.model.Bill;
import com.luckia.biller.core.model.CommonState;
import com.luckia.biller.core.model.Liquidation;
import com.luckia.biller.core.model.Store;

public class TestCostCenterQuery {

	@Test
	public void test() {
		try {
			Injector injector = Guice.createInjector(new BillerModule());
			injector.getInstance(PersistService.class).start();
			EntityManager entityManager = injector.getProvider(EntityManager.class).get();

			StringBuffer sb = new StringBuffer();
			sb.append("select distinct(l) from Liquidation l ");
			sb.append("join Bill b on b.liquidation =  l ");
			sb.append("join Store s on b.sender = s ");
			sb.append("where l.currentState.stateDefinition.id in :states ");
			sb.append("and s.costCenter.id = 11 ");
			sb.append("order by l.billDate desc, l.id");

			TypedQuery<Liquidation> query = entityManager.createQuery(sb.toString(), Liquidation.class);
			query.setParameter("states", Arrays.asList(CommonState.CONFIRMED, CommonState.SENT));
			query.setMaxResults(5);
			List<Liquidation> list = query.getResultList();
			for (Liquidation i : list) {
				System.out.println(String.format("%-40s %-20s %-20s", i.getSender().getName(), i.getBillDate(), i.getCurrentState().getStateDefinition().getId()));
				for (Bill bill : i.getBills()) {
					System.out.println(String.format("%-40s %-20s", "", bill.getSender().as(Store.class).getCostCenter().getName()));
				}
				// System.out.println(serializer.toJson(i));
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
