package com.luckia.biller.core.jpa;

import java.util.List;

import javax.inject.Provider;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.junit.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.persist.PersistService;
import com.luckia.biller.core.BillerModule;
import com.luckia.biller.core.common.ASTNode;
import com.luckia.biller.core.model.Bill;

public class TestFiqlParser {

	@Test
	public void test() {
		Injector injector = Guice.createInjector(new BillerModule());
		injector.getInstance(PersistService.class).start();
		Provider<EntityManager> entityManagerProvider = injector.getProvider(EntityManager.class);
		Class<Bill> entityClass = Bill.class;
		EntityManager entityManager = entityManagerProvider.get();
		CriteriaBuilder builder = entityManager.getCriteriaBuilder();
		CriteriaQuery<Bill> criteria = builder.createQuery(entityClass);
		Root<Bill> root = criteria.from(entityClass);

		String expression;
		expression = "(owner.id==1,owner.id==2);currentState.stateDefinition.id=='BillDraft'";
		expression = "currentState.stateDefinition.id==BillCancelled";
		expression = "currentState.stateDefinition.id==BillCancelled,code==000200,code==000100";
		expression = "amount=ge=100";
		expression = "code=!n=";
		expression = "code=n=";

		FiqlParser parser = new FiqlParser();
		ASTNode<Predicate> result = parser.parse(expression, builder, root);
		Predicate predicate = result.build();
		System.out.println(result);
		System.out.println(predicate);

		criteria.where(predicate);
		TypedQuery<Bill> query = entityManager.createQuery(criteria);
		List<Bill> list = query.getResultList();
		for (Bill bill : list) {
			System.out.println(bill);
		}
	}

}
