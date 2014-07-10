package com.luckia.biller.core.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.luckia.biller.core.model.Store;
import com.luckia.biller.core.model.validation.ValidStore;

public class StoreValidator implements ConstraintValidator<ValidStore, Store> {
	
	private static final Logger LOG = LoggerFactory.getLogger(StoreValidator.class);

	@Override
	public void initialize(ValidStore constraintAnnotation) {
	}

	@Override
	public boolean isValid(Store entity, ConstraintValidatorContext context) {
		boolean hasErrors = false;
		if (entity.getBillingModel() == null) {
			LOG.warn("Invalid store: model is null");
			context.buildConstraintViolationWithTemplate("store.missing.model").addConstraintViolation();
			hasErrors = true;
		}
		if (entity.getOwner() == null) {
			LOG.warn("Invalid store: owner is null");
			context.buildConstraintViolationWithTemplate("store.missing.owner").addConstraintViolation();
			hasErrors = true;
		}
		return !hasErrors;
	}

}
