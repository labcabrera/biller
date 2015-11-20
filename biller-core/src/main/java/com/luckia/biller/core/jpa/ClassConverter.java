/*******************************************************************************
 * Copyright (c) 2004, 2012 Kotasoft S.L.
 * All rights reserved. This program and the accompanying materials
 * may only be used prior written consent of Kotasoft S.L.
 ******************************************************************************/
package com.luckia.biller.core.jpa;

import org.eclipse.persistence.mappings.DatabaseMapping;
import org.eclipse.persistence.mappings.converters.Converter;
import org.eclipse.persistence.sessions.Session;

/**
 * {@link Converter} encargado de serializar los tipos <code>Class</code> a partir de su representacion en String.
 */
@SuppressWarnings("serial")
public class ClassConverter implements Converter {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.persistence.mappings.converters.Converter# convertObjectValueToDataValue(java.lang.Object, org.eclipse.persistence.sessions.Session)
	 */
	@Override
	public Object convertObjectValueToDataValue(Object objectValue, Session session) {
		Object result = null;
		if (objectValue != null) {
			if (Class.class.isAssignableFrom(objectValue.getClass())) {
				result = ((Class<?>) objectValue).getName();
			} else {
				throw new IllegalArgumentException("Invalid type: " + objectValue);
			}
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.persistence.mappings.converters.Converter# convertDataValueToObjectValue(java.lang.Object, org.eclipse.persistence.sessions.Session)
	 */
	@Override
	public Object convertDataValueToObjectValue(Object dataValue, Session session) {
		Object result = null;
		if (dataValue != null) {
			if (String.class.isAssignableFrom(dataValue.getClass())) {
				try {
					result = Class.forName(dataValue.toString());
				} catch (Exception ex) {
					throw new IllegalArgumentException("Invalid class: " + dataValue);
				}
			} else {
				throw new IllegalArgumentException("Invalid type: " + dataValue);
			}
		}
		return result;
	}

	@Override
	public boolean isMutable() {
		return false;
	}

	@Override
	public void initialize(DatabaseMapping mapping, Session session) {
	}
}
