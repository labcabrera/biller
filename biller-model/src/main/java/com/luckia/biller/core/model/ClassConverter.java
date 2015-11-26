package com.luckia.biller.core.model;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

/**
 * {@link Converter} encargado de serializar los tipos <code>Class</code> a partir de su representacion en String.
 */
@SuppressWarnings("rawtypes")
@Converter
public class ClassConverter implements AttributeConverter<Class, String> {

	@Override
	public String convertToDatabaseColumn(Class clazz) {
		return clazz.getSimpleName();
	}

	@Override
	public Class convertToEntityAttribute(String value) {
		try {
			return Class.forName(value);
		} catch (Exception ex) {
			throw new RuntimeException("Invalid class name: " + value, ex);
		}
	}

}
