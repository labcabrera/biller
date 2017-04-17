package com.luckia.biller.web.rest;

import java.net.URI;
import java.net.URISyntaxException;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.persistence.EntityManager;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.StringUtils;

import com.luckia.biller.core.common.BillerException;
import com.luckia.biller.core.model.UserSession;

public abstract class AbstractBinaryRestService {

	protected final static String REDIRECT_NO_CONTENT_URI = "../#/204";
	protected final static String REDIRECT_FORBIDDEN_URI = "../#/403";

	@Inject
	protected Provider<EntityManager> entityManagerProvider;

	protected boolean checkSessionId(String sessionId) {
		return StringUtils.isNotBlank(sessionId)
				&& entityManagerProvider.get().find(UserSession.class, sessionId) != null;
	}

	protected Response sendRedirect(String uri) {
		try {
			return Response.temporaryRedirect(new URI(uri)).build();
		}
		catch (URISyntaxException ex) {
			throw new BillerException("Send redirection error", ex);
		}
	}
}
