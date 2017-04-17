package com.luckia.biller.web.rest;

import java.util.Date;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.persistence.EntityManager;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.apache.commons.lang3.Range;
import org.joda.time.DateTime;

import com.luckia.biller.core.common.RegisterActivity;
import com.luckia.biller.core.model.Company;
import com.luckia.biller.core.model.Store;
import com.luckia.biller.core.model.UserActivityType;
import com.luckia.biller.core.model.common.Message;
import com.luckia.biller.core.services.bills.recalculation.BillRecalculationInfo;
import com.luckia.biller.core.services.bills.recalculation.BillRecalculationService;

@Path("recalculation")
public class RecalculationRestService {

	@Inject
	private Provider<EntityManager> entityManagerProvider;
	@Inject
	private BillRecalculationService billRecalculationService;

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/prepare/bill")
	public Message<BillRecalculationInfo> prepare(@QueryParam("s") Long storeId,
			@QueryParam("c") Long companyId, @QueryParam("y") Integer year,
			@QueryParam("m") Integer month) {
		if (year == null || month == null) {
			return new Message<BillRecalculationInfo>()
					.withCode(Message.CODE_GENERIC_ERROR)
					.addWarning("billRecalculation.missingYearMonth");
		}
		Range<Date> range = getEffectiveRange(year, month);
		EntityManager entityManager = entityManagerProvider.get();
		Store store = null;
		Company company = null;
		if (storeId != null) {
			store = entityManager.find(Store.class, storeId);
		}
		if (companyId != null) {
			company = entityManager.find(Company.class, companyId);
		}
		return billRecalculationService.prepare(company, store, range);
	}

	/**
	 * Genera una nueva factura o la regenera si ya existe a partir del establecimiento y
	 * la fecha.
	 * 
	 * @param storeId
	 * @param year
	 * @param month
	 * @return
	 */
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/execute/bill")
	@RegisterActivity(type = UserActivityType.BILL_RECALCULATION)
	public Message<BillRecalculationInfo> executeBill(BillRecalculationInfo info) {
		return billRecalculationService.execute(info);
	}

	private Range<Date> getEffectiveRange(Integer year, Integer month) {
		DateTime from = new DateTime(year, month, 1, 0, 0, 0, 0);
		DateTime to = from.dayOfMonth().withMaximumValue();
		return Range.between(from.toDate(), to.toDate());
	}

}
