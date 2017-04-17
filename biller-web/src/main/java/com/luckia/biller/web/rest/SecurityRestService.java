package com.luckia.biller.web.rest;

import java.util.Map;

import javax.annotation.security.PermitAll;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.luckia.biller.core.model.common.Message;
import com.luckia.biller.core.services.security.UserSessionService;

@Path("/security")
public class SecurityRestService {

	private static final Logger LOG = LoggerFactory.getLogger(SecurityRestService.class);

	@Inject
	private UserSessionService userSessionService;

	@POST
	@Path("/login")
	@Consumes(MediaType.APPLICATION_JSON)
	@PermitAll
	public Message<Map<String, Object>> login(JsonElement request) {
		LOG.debug("Received login request");
		try {
			String key = request.getAsJsonObject().get("user").getAsString();
			String password = request.getAsJsonObject().get("password").getAsString();
			return userSessionService.login(key, password);
		}
		catch (Exception ex) {
			LOG.error("Login request error", ex);
			throw new InternalServerErrorException("Login error", ex);
		}
	}

	@POST
	@Path("/logout")
	@PermitAll
	public Message<String> logout(String sessionId) {
		LOG.debug("Received logout request");
		try {
			userSessionService.logout(sessionId);
			return new Message<String>().withMessage("Success");
		}
		catch (Exception ex) {
			LOG.error("Logout request error", ex);
			throw new InternalServerErrorException("Logout error", ex);
		}
	}
}
