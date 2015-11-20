package com.luckia.biller.core.validation;

import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import org.junit.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.luckia.biller.core.BillerModule;
import com.luckia.biller.core.model.Address;
import com.luckia.biller.core.model.IdCard;
import com.luckia.biller.core.model.IdCardType;
import com.luckia.biller.core.model.LegalEntity;
import com.luckia.biller.core.model.Province;
import com.luckia.biller.core.model.Region;

public class TestLegalEntityValidation {

	@Test
	public void test() {
		Locale locale = new Locale("es", "ES");
		ResourceBundle messages = ResourceBundle.getBundle("com.luckia.biller.core.i18n.messages", locale);
		Injector injector = Guice.createInjector(new BillerModule());
		Validator validator = injector.getInstance(Validator.class);
		LegalEntity legalEntity = new LegalEntity();
		legalEntity.setName("name");
		legalEntity.setIdCard(new IdCard(IdCardType.CIF, "number"));
		legalEntity.setAddress(new Address());
		legalEntity.getAddress().setProvince(new Province());
		legalEntity.getAddress().getProvince().setId("1");
		legalEntity.getAddress().setRegion(new Region());
		legalEntity.getAddress().getRegion().setProvince(new Province());
		legalEntity.getAddress().getRegion().getProvince().setId("2");
		Set<ConstraintViolation<LegalEntity>> violations = validator.validate(legalEntity);
		System.out.println(violations);
		if (!violations.isEmpty()) {
			for (ConstraintViolation<LegalEntity> i : violations) {
				String key = i.getMessage();
				System.out.println(key + ": " + messages.getString(key));
			}
		}
	}
}
