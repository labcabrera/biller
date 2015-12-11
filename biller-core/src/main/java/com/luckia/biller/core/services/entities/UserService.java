package com.luckia.biller.core.services.entities;

import java.security.MessageDigest;

import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.Validate;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.persist.Transactional;
import com.luckia.biller.core.common.RegisterActivity;
import com.luckia.biller.core.model.User;
import com.luckia.biller.core.model.UserActivityType;
import com.luckia.biller.core.model.common.Message;

public class UserService extends EntityService<User> {

	private static final Logger LOG = LoggerFactory.getLogger(UserService.class);

	@Inject
	@Named("security.password-digest-algoritm")
	private String passwordDigestAlgoritm;

	@Override
	@Transactional
	@RegisterActivity(type = UserActivityType.USER_MERGE)
	public Message<User> merge(User user) {
		LOG.debug("Processing user merge");
		Message<User> message = new Message<>();
		try {
			EntityManager entityManager = entityManagerProvider.get();
			if (user.getId() != null) {
				User current = entityManager.find(User.class, user.getId());
				current.merge(user);
				entityManager.merge(current);
				message.addInfo("user.merge.success").withPayload(current);
			} else {
				String digest = calculatePasswordDigest(user.getPasswordDigest());
				user.setPasswordDigest(digest);
				user.setCreated(new DateTime().toDate());
				entityManager.persist(user);
				entityManager.flush();
				message.addInfo("user.insert.success").withPayload(user);
			}
		} catch (Exception ex) {
			LOG.error("User merge error");
			message.withCode(Message.CODE_GENERIC_ERROR).addError("user.merge.error");
		}
		return message;
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

	@Override
	protected Class<User> getEntityClass() {
		return User.class;
	}

}
