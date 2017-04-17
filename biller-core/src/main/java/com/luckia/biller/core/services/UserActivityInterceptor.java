package com.luckia.biller.core.services;

import javax.inject.Inject;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.persist.Transactional;
import com.luckia.biller.core.common.RegisterActivity;
import com.luckia.biller.core.model.UserActivityType;

public class UserActivityInterceptor implements MethodInterceptor {

	private static final Logger LOG = LoggerFactory
			.getLogger(UserActivityInterceptor.class);

	@Inject
	private AuditService auditService;

	@Override
	@Transactional
	public Object invoke(MethodInvocation e) throws Throwable {
		RegisterActivity annotation = e.getMethod().getAnnotation(RegisterActivity.class);
		UserActivityType type = annotation.type();
		LOG.debug("Register user activity {}", type);
		Object[] args = e.getArguments();
		Object data = null;
		if (args != null) {
			switch (args.length) {
			case 1:
				data = args[0];
				break;
			default:
				data = args;
				break;
			}
		}
		auditService.addUserActivity(type, data);
		return e.proceed();
	}

}
