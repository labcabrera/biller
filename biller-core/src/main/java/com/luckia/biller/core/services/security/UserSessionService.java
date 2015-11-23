package com.luckia.biller.core.services.security;

import java.security.MessageDigest;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.Validate;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.persist.Transactional;
import com.luckia.biller.core.model.User;
import com.luckia.biller.core.model.UserSession;
import com.luckia.biller.core.model.common.Message;

public class UserSessionService {

	private static final Logger LOG = LoggerFactory.getLogger(UserSessionService.class);

	@Inject	
	private Provider<EntityManager> entityManagerProvider;

	@Inject
	@Named("security.password-digest-algoritm")
	private String passwordDigestAlgoritm;

	@Inject
	@Named("security.session-expiration-minutes")
	private Integer sessionExpiration;

	public Message<Map<String, Object>> login(String key, String password) {
		Message<Map<String, Object>> result = new Message<>();
		EntityManager entityManager = entityManagerProvider.get();
		TypedQuery<User> query = entityManager.createNamedQuery("User.login", User.class);
		List<User> users = query.setParameter("key", key).getResultList();
		if (users.isEmpty()) {
			return result.withCode("404").addError("login.invalid.user");
		}
		for (User user : users) {
			String digest = calculatePasswordDigest(password);
			if (digest.equals(user.getPasswordDigest())) {
				UserSession session = createSession(user);
				Map<String, Object> map = new LinkedHashMap<>();
				map.put("name", user.getName());
				map.put("alias", user.getAlias());
				map.put("email", user.getEmail());
				map.put("session", session.getSession());
				map.put("roles", user.getRoles());
				return result.addInfo("login.success").withPayload(map);
			}
		}
		return result.withCode("401").addError("login.invalid.password");
	}

	public Message<Boolean> validateSession(String sessionId) {
		Message<Boolean> result = new Message<>();
		EntityManager entityManager = entityManagerProvider.get();
		TypedQuery<Date> query = entityManager.createNamedQuery("User.validateSession", Date.class);
		try {
			Date expiration = query.setParameter("sessionId", sessionId).getSingleResult();
			if (expiration == null || expiration.before(Calendar.getInstance().getTime())) {
				return result.withPayload(true);
			} else {
				return result.withPayload(false).withCode("501").addError("Expired session");
			}
		} catch (NoResultException ex) {
			return result.withPayload(false).withCode("502").addError("Missing session");
		}

	}

	@Transactional
	public UserSession createSession(User user) {
		EntityManager entityManager = entityManagerProvider.get();
		entityManager.createNamedQuery("User.deleteSession").setParameter("user", user).executeUpdate();
		UserSession session = new UserSession();
		Date now = new DateTime().toDate();
		session.setSession(UUID.randomUUID().toString());
		session.setUser(user);
		session.setCreated(now);
		session.setLastAccess(session.getCreated());
		session.setExpiration(calculateExpiration(now));
		entityManager.persist(session);
		return session;
	}

	@Transactional
	public void touchSession(String sessionId) {
		EntityManager entityManager = entityManagerProvider.get();
		Date lastAccess = new DateTime().toDate();
		Date expiration = calculateExpiration(lastAccess);
		Query query = entityManager.createNamedQuery("User.touchSession");
		query.setParameter("sessionId", sessionId);
		query.setParameter("lastAccess", lastAccess);
		query.setParameter("expiration", expiration);
		query.executeUpdate();
		entityManager.flush();
	}

	@Transactional
	public void logout(String sessionId) {
		EntityManager entityManager = entityManagerProvider.get();
		Query query = entityManager.createQuery("delete from UserSession e where e.session = :sessionId");
		query.setParameter("sessionId", sessionId).executeUpdate();
	}

	public String calculatePasswordDigest(String password) {
		try {
			Validate.notNull(passwordDigestAlgoritm, "Invalid configuration digest password algoritm");
			MessageDigest digest = MessageDigest.getInstance(passwordDigestAlgoritm);
			digest.reset();
			byte[] rawDigest = digest.digest(password.getBytes("UTF-8"));
			return new String(Hex.encodeHex(rawDigest));
		} catch (Exception ex) {
			LOG.error("Digest calculation error", ex);
			throw new RuntimeException(ex);
		}
	}

	private Date calculateExpiration(Date date) {
		return sessionExpiration != null && sessionExpiration > 0 ? new DateTime(date).plusMinutes(sessionExpiration).toDate() : null;
	}

}
