package com.luckia.biller.core.validation;

import java.math.RoundingMode;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import com.luckia.biller.core.common.MathUtils;
import com.luckia.biller.core.model.Rappel;
import com.luckia.biller.core.model.validation.ValidRappel;

public class RappelValidator implements ConstraintValidator<ValidRappel, Rappel> {

	@Override
	public void initialize(ValidRappel constraintAnnotation) {
	}

	@Override
	public boolean isValid(Rappel entity, ConstraintValidatorContext context) {
		boolean hasErrors = false;

		if (entity.getModel() == null || entity.getModel().getId() == null) {
			context.buildConstraintViolationWithTemplate("rappel.missing.model").addConstraintViolation();
			hasErrors = true;
		}
		if (MathUtils.isZero(entity.getAmount())) {
			context.buildConstraintViolationWithTemplate("rappel.missing.amount").addConstraintViolation();
			hasErrors = true;
		} else if (!MathUtils.isNotZeroPositive(entity.getAmount())) {
			context.buildConstraintViolationWithTemplate("rappel.invalid.amount").addConstraintViolation();
			hasErrors = true;
		} else {
			entity.setAmount(entity.getAmount().setScale(2, RoundingMode.HALF_EVEN));
		}
		if (MathUtils.isZero(entity.getBonusAmount())) {
			context.buildConstraintViolationWithTemplate("rappel.missing.bonusAmount").addConstraintViolation();
			hasErrors = true;
		} else if (!MathUtils.isNotZeroPositive(entity.getBonusAmount())) {
			context.buildConstraintViolationWithTemplate("rappel.invalid.bonusAmount").addConstraintViolation();
			hasErrors = true;
		} else {
			entity.setBonusAmount(entity.getBonusAmount().setScale(2, RoundingMode.HALF_EVEN));
		}
		return !hasErrors;
	}

}
