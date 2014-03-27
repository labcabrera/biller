/*******************************************************************************
 * Copyright (c) 2004, 2012 Kotasoft S.L.
 * All rights reserved. This program and the accompanying materials
 * may only be used prior written consent of Kotasoft S.L.
 ******************************************************************************/
package com.luckia.biller.core.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.apache.commons.lang3.StringUtils;

import com.luckia.biller.core.model.CostCenter;
import com.luckia.biller.core.model.validation.ValidCostCenter;

public class CostCenterValidator implements ConstraintValidator<ValidCostCenter, CostCenter> {

	@Override
	public void initialize(ValidCostCenter constraintAnnotation) {
	}

	@Override
	public boolean isValid(CostCenter entity, ConstraintValidatorContext context) {
		Boolean valid = true;
		if (StringUtils.isBlank(entity.getCode())) {
			context.buildConstraintViolationWithTemplate("costCenter.code.required").addConstraintViolation();
			valid = false;
		}
		return valid;
	}
}
