package com.luckia.biller.core.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.luckia.biller.core.model.Address;
import com.luckia.biller.core.model.LegalEntity;
import com.luckia.biller.core.model.validation.ValidLegalEntity;

public class LegalEntityValidator
		implements ConstraintValidator<ValidLegalEntity, LegalEntity> {

	private static final Logger LOG = LoggerFactory.getLogger(LegalEntityValidator.class);

	@Override
	public void initialize(ValidLegalEntity constraintAnnotation) {
	}

	@Override
	public boolean isValid(LegalEntity entity, ConstraintValidatorContext context) {
		LOG.info("Validando entidad legal {}", entity);
		boolean hasErrors = false;
		try {
			if (StringUtils.isBlank(entity.getName())) {
				LOG.error("La entidad carece de nombre");
				context.buildConstraintViolationWithTemplate("legalEntity.name.required")
						.addConstraintViolation();
				hasErrors = true;
			}
			if (entity.getIdCard() == null
					|| StringUtils.isBlank(entity.getIdCard().getNumber())) {
				LOG.trace("La entidad carece de numero de identificacion fiscal");
				// context.buildConstraintViolationWithTemplate("legalEntity.idCard.required").addConstraintViolation();
				// valid = false;
			}
			if (entity.getAddress() != null) {
				Address address = entity.getAddress();
				String provinceA = address.getProvince() != null
						? address.getProvince().getId() : null;
				String provinceB = address.getRegion() != null
						&& address.getRegion().getProvince() != null
								? address.getRegion().getProvince().getId() : null;
				if (provinceA != null && provinceB != null
						&& !provinceA.equals(provinceB)) {
					LOG.trace("No coincide la provincia con el municipio");
					// context.buildConstraintViolationWithTemplate("legalEntity.address.provinceNotMatches").addConstraintViolation();
					// valid = false;
				}
			}
		}
		catch (Exception ex) {
			hasErrors = true;
			LOG.error("Error durante la validacion de la entidad legal", ex);
		}
		return !hasErrors;
	}
}
