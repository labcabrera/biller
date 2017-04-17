package com.luckia.biller.core.model.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

import com.luckia.biller.core.validation.CostCenterValidator;

/**
 * Definición de la validacin de un centro de coste
 */
@Target({ ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE,
		ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = CostCenterValidator.class)
@Documented
public @interface ValidCostCenter {

	String message() default "costCenter.invalid";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};
}
