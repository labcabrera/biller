package com.luckia.biller.web.rest;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;

import com.luckia.biller.core.common.MathUtils;
import com.luckia.biller.core.i18n.I18nService;
import com.luckia.biller.core.model.Liquidation;
import com.luckia.biller.core.model.common.Message;
import com.luckia.biller.web.model.ChartModel;

@Path("/dashboard")
@Consumes({ "application/json; charset=UTF-8" })
@Produces({ "application/json; charset=UTF-8" })
public class DashboardRestService {

	private static final Integer MAX_PIE_RESULTS = 12;

	@Inject
	private Provider<EntityManager> entityManagerProvider;
	@Inject
	private I18nService i18nService;

	@GET
	@Path("/company/evolution")
	public List<ChartModel> contractEvolutionAmount(@QueryParam("companyId") Long companyId, @QueryParam("year") Integer year) {
		year = year != null && year > 0 ? year : new DateTime().getYear();
		EntityManager entityManager = entityManagerProvider.get();
		List<ChartModel> list = new ArrayList<>();
		StringBuffer sb = new StringBuffer();
		sb.append("SELECT SUM(L.LIQUIDATION_EFFECTIVE_AMOUNT) FROM B_LIQUIDATION L ");
		sb.append("JOIN B_ABSTRACT_BILL AB ON L.ID = AB.ID ");
		sb.append("JOIN S_STATE S ON AB.CURRENT_STATE = S.ID ");
		sb.append("WHERE AB.RECEIVER = ? ");
		sb.append("AND AB.BILL_DATE BETWEEN ? AND ? ");
		sb.append("AND S.STATE_DEFINITION_ID IN ('Sent', 'Confirmed')");
		Query query = entityManager.createNativeQuery(sb.toString());
		for (int i = 1; i < 13; i++) {
			DateTime from = new DateTime(year, i, 1, 0, 0, 0);
			DateTime to = from.dayOfMonth().withMaximumValue();
			query.setParameter(1, companyId);
			query.setParameter(2, from.toDate());
			query.setParameter(3, to.toDate());
			BigDecimal partial = MathUtils.safeNull((BigDecimal) query.getResultList().iterator().next());
			String monthI18nKey = "month." + StringUtils.leftPad(String.valueOf(i), 2, "0");
			list.add(new ChartModel(i18nService.getMessage(monthI18nKey), partial.doubleValue()));
		}
		return list;
	}

	@GET
	@Path("company/storeDistribution")
	public List<ChartModel> companyStoreDistributionByMonth(@QueryParam("companyId") Long companyId, @QueryParam("year") Integer year, @QueryParam("month") Integer month,
			@QueryParam("negative") Boolean negative) {
		year = year != null ? year : new DateTime().getYear();
		month = month != null ? month + 1 : new DateTime().getMonthOfYear();
		DateTime from = new DateTime(year, month, 1, 0, 0, 0);
		DateTime to = from.dayOfMonth().withMaximumValue();
		return companyDistribution(companyId, from, to, negative);
	}

	@GET
	@Path("company/storeDistribution/negative")
	public List<ChartModel> companyStoreDistributionNegative(@QueryParam("companyId") Long companyId, @QueryParam("year") Integer year, @QueryParam("month") Integer month) {
		return companyStoreDistributionByMonth(companyId, year, month, true);
	}

	@GET
	@Path("company/storeDistributionAnual")
	public List<ChartModel> companyStoreDistributionByYear(@QueryParam("companyId") Long companyId, @QueryParam("year") Integer year, @QueryParam("negative") Boolean negative) {
		year = year != null ? year : new DateTime().getYear();
		DateTime from = new DateTime(year, 1, 1, 0, 0, 0);
		DateTime to = new DateTime(year, 12, 31, 0, 0, 0);
		return companyDistribution(companyId, from, to, negative);
	}

	@GET
	@Path("company/storeDistributionAnual/negative")
	public List<ChartModel> companyStoreDistributionByYearNegative(@QueryParam("companyId") Long companyId, @QueryParam("year") Integer year) {
		return companyStoreDistributionByYear(companyId, year, true);
	}

	private List<ChartModel> companyDistribution(Long companyId, DateTime from, DateTime to, Boolean negative) {
		StringBuffer sb = new StringBuffer();
		sb.append("SELECT LE.NAME, L.LIQUIDATION_EFFECTIVE_AMOUNT FROM B_LIQUIDATION L ");
		sb.append("JOIN B_ABSTRACT_BILL AB ON L.ID = AB.ID ");
		sb.append("JOIN B_LEGAL_ENTITY LE ON AB.SENDER = LE.ID ");
		sb.append("JOIN S_STATE S ON AB.CURRENT_STATE = S.ID ");
		sb.append("WHERE AB.RECEIVER = ? ");
		sb.append("AND AB.BILL_DATE BETWEEN ? AND ? ");
		sb.append("AND S.STATE_DEFINITION_ID IN ('Sent', 'Confirmed') ");
		if (negative == null || !negative) {
			sb.append("AND L.LIQUIDATION_EFFECTIVE_AMOUNT > 0 ");
			sb.append("GROUP BY LE.NAME ");
			sb.append("ORDER BY L.LIQUIDATION_EFFECTIVE_AMOUNT DESC ");
		} else {
			sb.append("AND L.LIQUIDATION_EFFECTIVE_AMOUNT < 0 ");
			sb.append("GROUP BY LE.NAME ");
			sb.append("ORDER BY L.LIQUIDATION_EFFECTIVE_AMOUNT");
		}
		EntityManager entityManager = entityManagerProvider.get();
		Query query = entityManager.createNativeQuery(sb.toString());
		query.setParameter(1, companyId);
		query.setParameter(2, from.toDate());
		query.setParameter(3, to.toDate());
		List<ChartModel> list = new ArrayList<>();
		List<?> results = query.getResultList();
		ChartModel others = new ChartModel("Otros", 0L);
		int index = 0;
		for (Object i : results) {
			Object[] raw = (Object[]) i;
			String name = (String) raw[0];
			BigDecimal partial = (BigDecimal) raw[1];
			if (index > MAX_PIE_RESULTS) {
				others.setValue(others.getValue() + partial.doubleValue());
				if (!list.contains(others)) {
					list.add(others);
				}
			} else {
				list.add(new ChartModel(name, partial.doubleValue()));
			}
			index++;
		}
		return list;
	}

	// TODO filtrar
	@GET
	@Path("liquidation/pending")
	public Message<List<Liquidation>> liquidationPending(@QueryParam("n") Integer n, @QueryParam("p") Integer p) {
		EntityManager entityManager = entityManagerProvider.get();
		TypedQuery<Liquidation> query = entityManager.createQuery("select e from Liquidation e", Liquidation.class);
		query.setMaxResults(10);
		List<Liquidation> list = query.getResultList();
		return new Message<List<Liquidation>>().withPayload(list);
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

	private TypedQuery<ChartModel> getQuery(String sql, Integer year) {
		TypedQuery<ChartModel> query = entityManagerProvider.get().createQuery(String.format(sql, ChartModel.class.getName()), ChartModel.class);
		if (year != null && year != -1) {
			query.setParameter("date1", new DateTime(year, 1, 1, 0, 0).toDate());
			query.setParameter("date2", new DateTime(year, 12, 31, 0, 0).toDate());
		}
		return query;
	}
}