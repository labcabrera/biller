package com.luckia.biller.web.rest;

import java.util.Map;

import javax.annotation.security.PermitAll;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;

import com.google.gson.JsonElement;
import com.luckia.biller.core.model.common.Message;
import com.luckia.biller.core.services.security.UserSessionService;

@Path("/security")
public class SecurityRestService {

	@Inject
	private UserSessionService userSessionService;

	@POST
	@Path("/login")
	@Consumes(MediaType.APPLICATION_JSON)
	@PermitAll
	public Message<Map<String, String>> login(JsonElement request) {
		String key = request.getAsJsonObject().get("user").getAsString();
		String password = request.getAsJsonObject().get("password").getAsString();
		return userSessionService.login(key, password);
	}

	@POST
	@Path("/logout")
	@PermitAll
	public Message<String> logout(String sessionId) {
		userSessionService.logout(sessionId);
		return new Message<String>().withMessage("Success");
	}
}
