package com.luckia.biller.deploy.poi;

import org.apache.commons.lang3.mutable.Mutable;

public class PersonNameResolver {

	public void resolve(String value, Mutable<String> name, Mutable<String> firstSurname,
			Mutable<String> secondSurname) {
		name.setValue(value);
	}

}
