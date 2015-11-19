package com.luckia.biller.web.rest;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.joda.time.DateTime;

import com.luckia.biller.web.model.ChartModel;

@Path("/dashboard")
@Consumes({ "application/json; charset=UTF-8" })
@Produces({ "application/json; charset=UTF-8" })
public class DashboardRestService {

	@Inject
	private Provider<EntityManager> entityManagerProvider;

	@GET
	@Path("/company/evolution/amount")
	public List<ChartModel> contractEvolutionAmount() {
		List<ChartModel> list = new ArrayList<>();
		list.add(new ChartModel("Enero", 12567d));
		list.add(new ChartModel("Febrero", 9843d));
		list.add(new ChartModel("Marzo", 16343d));
		list.add(new ChartModel("Abril", 6789d));
		list.add(new ChartModel("Mayo", 11335d));
		list.add(new ChartModel("Junio", 13335d));
		list.add(new ChartModel("Agosto", 14335d));
		list.add(new ChartModel("Septiembre", 14335d));
		list.add(new ChartModel("Octubre", 0d));
		list.add(new ChartModel("Noviembre", 0d));
		list.add(new ChartModel("Diciembre", 0d));
		return list;
	}
	@GET
	@Path("company/storeDistribution")
	public List<ChartModel> companyStoreDistribution() {
		List<ChartModel> list = new ArrayList<>();
		list.add(new ChartModel("Bar PEPE", 12567d));
		list.add(new ChartModel("Bar MANOLO", 9843d));
		list.add(new ChartModel("Bar ANTONIO", 16343d));
		list.add(new ChartModel("Bar Pro", 26789d));
		list.add(new ChartModel("Cafe Real", 11335d));
		list.add(new ChartModel("Cafe Madrid", 13335d));
		list.add(new ChartModel("Luckia Sport Cafe", 14335d));
		list.add(new ChartModel("Centro de juego", 14335d));
		list.add(new ChartModel("Bar X", 345d));
		list.add(new ChartModel("Bar Y", 3233d));
		list.add(new ChartModel("Bar X", 100d));
		return list;
	}
	
	

	@GET
	@Path("/contract/agreement")
	public List<ChartModel> chartContractByAgreement(@QueryParam("year") Integer year) {
		String sql = "select new %s(e.agreement.name, COUNT(e.agreement.name)) from Contract e ";
		if (year != null && year != -1) {
			sql += "where s.effective between :date1 AND :date2 ";
		}
		sql += "group by e.agreement.name order by count(e.agreement.name) desc";
		return getQuery(sql, year).getResultList();
	}

	@GET
	@Path("/contract/state")
	public List<ChartModel> chartContractByState(@QueryParam("year") Integer year) {
		String sql = "select new %s(e.currentState.stateDefinition.name, COUNT(e.currentState.stateDefinition.name)) from Contract e ";
		if (year != null && year != -1) {
			sql += "where e.effective between :date1 AND :date2 ";
		}
		sql += "group by e.currentState.stateDefinition.name ORDER BY COUNT(e.currentState.stateDefinition.name) DESC";
		List<ChartModel> list = getQuery(sql, year).getResultList();
		for (ChartModel i : list) {
			i.setLabel("contractState." + i.getLabel());
		}
		return list;
	}

	@GET
	@Path("/sinister/state")
	public List<ChartModel> sinisterState(@QueryParam("year") Integer year) {
		String sql = "SELECT NEW %s(s.state, COUNT(s.state)) FROM Sinister s ";
		if (year != null && year != -1) {
			sql += "WHERE s.sinisterDate BETWEEN :date1 AND :date2 ";
		}
		sql += "GROUP BY s.state ORDER BY COUNT(s.state) DESC";
		return getQuery(sql, year).getResultList();
	}

	@GET
	@Path("/sinister/evolution")
	public List<ChartModel> sinisterEvolution(@QueryParam("year") Integer year) {
		String sql = null;
		if (year == null || year == -1) {
			sql = "SELECT NEW %s(FUNC('YEAR', s.sinisterDate), COUNT(s)) FROM Sinister s ";
			sql += "GROUP BY FUNC('YEAR', s.sinisterDate) ORDER BY FUNC('YEAR', s.sinisterDate)";
		} else {
			sql = "SELECT NEW %s(FUNC('MONTH', s.sinisterDate), COUNT(s)) FROM Sinister s ";
			sql += "WHERE s.sinisterDate BETWEEN :date1 AND :date2 GROUP BY FUNC('MONTH', s.sinisterDate)";
		}
		return getQuery(sql, year).getResultList();
	}

	@GET
	@Path("/payment/evolution")
	public List<ChartModel> paymentEvolution(@QueryParam("year") Integer year) {
		String sql = null;
		if (year == null || year == -1) {
			sql = "SELECT NEW %s(FUNC('YEAR', p.effectiveDate), SUM(p.amount)) FROM Payment p ";
			sql += "GROUP BY FUNC('YEAR', p.effectiveDate) ORDER BY FUNC('YEAR', p.effectiveDate)";
		} else {
			sql = "SELECT NEW %s(FUNC('MONTH', p.effectiveDate), SUM(p.amount)) FROM Payment p ";
			sql += "WHERE p.effectiveDate BETWEEN :date1 AND :date2 GROUP BY FUNC('MONTH', p.effectiveDate) ORDER BY FUNC('MONTH', p.effectiveDate)";
		}
		return getQuery(sql, year).getResultList();
	}

	// @GET
	// @Path("/alert/pendingPayment")
	// public SearchResults<PendingPayment> pendingPayment(@QueryParam("p") Integer page, @QueryParam("n") Integer maxResults) {
	// Long count = entityManagerProvider.get().createQuery("SELECT COUNT(p) FROM Payment p WHERE p.state = :state",
	// Long.class).setParameter("state", PaymentState.PENDING)
	// .getSingleResult();
	// String sql =
	// "SELECT NEW %s(s.id, s.number, s.policy.insured.name, p.effectiveDate, p.amount) FROM Payment p JOIN p.sinister s WHERE p.state = :state";
	// TypedQuery<PendingPayment> query = entityManagerProvider.get().createQuery(String.format(sql, PendingPayment.class.getName()),
	// PendingPayment.class);
	// query.setParameter("state", PaymentState.PENDING).setMaxResults(maxResults).setFirstResult(maxResults * (page - 1));
	// return new SearchResults<PendingPayment>(page, maxResults, count, query.getResultList());
	// }

	private TypedQuery<ChartModel> getQuery(String sql, Integer year) {
		TypedQuery<ChartModel> query = entityManagerProvider.get().createQuery(String.format(sql, ChartModel.class.getName()), ChartModel.class);
		if (year != null && year != -1) {
			query.setParameter("date1", new DateTime(year, 1, 1, 0, 0).toDate());
			query.setParameter("date2", new DateTime(year, 12, 31, 0, 0).toDate());
		}
		return query;
	}
}