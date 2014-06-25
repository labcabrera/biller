package com.luckia.biller.core.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import com.luckia.biller.core.model.Store;
import com.luckia.biller.core.model.validation.ValidStore;

public class StoreValidator implements ConstraintValidator<ValidStore, Store> {

	@Override
	public void initialize(ValidStore constraintAnnotation) {
	}

	@Override
	public boolean isValid(Store entity, ConstraintValidatorContext context) {
		boolean hasErrors = false;
		if (entity.getBillingModel() == null) {
			context.buildConstraintViolationWithTemplate("store.missing.model").addConstraintViolation();
			hasErrors = true;
		}
		if (entity.getOwner() == null) {
			context.buildConstraintViolationWithTemplate("store.missing.owner").addConstraintViolation();
			hasErrors = true;
		}
		if(entity.getTerminalRelations() == null || entity.getTerminalRelations().isEmpty()) {
			context.buildConstraintViolationWithTemplate("store.missing.terminals").addConstraintViolation();
		}
		return !hasErrors;
	}

}
