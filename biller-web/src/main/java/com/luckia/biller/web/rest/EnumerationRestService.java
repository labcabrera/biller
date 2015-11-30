package com.luckia.biller.web.rest;

import java.util.Arrays;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import com.luckia.biller.core.model.CommonState;
import com.luckia.biller.core.model.UserActivityType;

@Path("/enumeration")
@Consumes({ "application/json; charset=UTF-8" })
@Produces({ "application/json; charset=UTF-8" })
public class EnumerationRestService {

	@GET
	@Path("/state")
	public List<CommonState> commonStates() {
		return Arrays.asList(CommonState.values());
	}

	@GET
	@Path("/userActivity")
	public List<UserActivityType> userActivitTypes() {
		return Arrays.asList(UserActivityType.values());
	}

	@GET
	@Path("/boolean")
	public List<String> booleanEnum() {
		return Arrays.asList("true", "false");
	}

}