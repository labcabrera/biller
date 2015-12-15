package com.luckia.biller.web.security;

import java.lang.reflect.Method;

import javax.annotation.security.PermitAll;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;

import org.apache.commons.lang3.StringUtils;
import org.jboss.resteasy.core.Headers;
import org.jboss.resteasy.core.ResourceMethodInvoker;
import org.jboss.resteasy.core.ServerResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.luckia.biller.core.model.User;
import com.luckia.biller.core.model.UserRole;
import com.luckia.biller.core.model.UserSession;
import com.luckia.biller.core.services.SecurityService;
import com.luckia.biller.core.services.security.RequiredRole;

@Provider
public class SecurityInterceptor implements ContainerRequestFilter {

	public static final ServerResponse ACCESS_FORBIDDEN = new ServerResponse("{\"message\":\"Access forbidden\"}", 403, new Headers<Object>());;
	private static final Logger LOG = LoggerFactory.getLogger(SecurityInterceptor.class);
	private static final String AUTHORIZATION_PROPERTY = "sessionid";

	@Inject
	private javax.inject.Provider<EntityManager> entityManagerProvider;
	@Inject
	private SecurityService securityService;

	@Override
	public void filter(ContainerRequestContext requestContext) {
		ResourceMethodInvoker methodInvoker = (ResourceMethodInvoker) requestContext.getProperty("org.jboss.resteasy.core.ResourceMethodInvoker");
		Method method = methodInvoker.getMethod();
		if (!method.isAnnotationPresent(PermitAll.class)) {
			final MultivaluedMap<String, String> headers = requestContext.getHeaders();
			EntityManager entityManager = entityManagerProvider.get();
			String sessionId = null;
			if (headers.containsKey(AUTHORIZATION_PROPERTY) && !headers.get(AUTHORIZATION_PROPERTY).isEmpty()) {
				sessionId = headers.get(AUTHORIZATION_PROPERTY).iterator().next();
			}
			if (StringUtils.isBlank(sessionId)) {
				LOG.debug("Missing sessionId");
				requestContext.abortWith(ACCESS_FORBIDDEN);
				return;
			}
			UserSession session = entityManager.find(UserSession.class, sessionId);
			if (session == null) {
				LOG.debug("Session not found");
				requestContext.abortWith(ACCESS_FORBIDDEN);
				return;
			}
			if (method.isAnnotationPresent(RequiredRole.class)) {
				User user = session.getUser();
				RequiredRole requiredRole = method.getAnnotation(RequiredRole.class);
				boolean validRole = false;
				for (String role : requiredRole.any()) {
					for (UserRole userRole : user.getRoles()) {
						if (role.equals(userRole.getCode())) {
							validRole = true;
							break;
						}
					}
				}
				if (!validRole) {
					LOG.debug("Invalid role groups");
					requestContext.abortWith(ACCESS_FORBIDDEN);
					return;
				}
			}
			securityService.setCurrentUser(session.getUser());
		}
	}
}
