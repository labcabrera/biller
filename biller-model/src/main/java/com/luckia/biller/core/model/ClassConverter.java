package com.luckia.biller.core.model;

import java.security.InvalidParameterException;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import lombok.extern.slf4j.Slf4j;

/**
 * {@link Converter} encargado de serializar los tipos <code>Class</code> a partir de su
 * representacion en String.
 */
@SuppressWarnings("rawtypes")
@Converter
@Slf4j
public class ClassConverter implements AttributeConverter<Class, String> {

	@Override
	public String convertToDatabaseColumn(Class clazz) {
		return clazz.getSimpleName();
	}

	@Override
	public Class convertToEntityAttribute(String value) {
		try {
			return Class.forName(value);
		}
		catch (Exception ex) {
			log.error("Invalid class", ex);
			throw new InvalidParameterException("Invalid class name: " + value);
		}
	}

}
