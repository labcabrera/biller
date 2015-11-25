package com.luckia.biller.core.serialization;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;

public class SerializationExclusionStrategy implements ExclusionStrategy {

	@Override
	public boolean shouldSkipField(FieldAttributes f) {
		// No incluimos los campos que se generan con eclipselink-weaving
		if (f.getName().startsWith("_")) {
			return true;
		}
		return f.getAnnotation(NotSerializable.class) != null;
	}

	@Override
	public boolean shouldSkipClass(Class<?> clazz) {
		return clazz.getAnnotation(NotSerializable.class) != null;
	}

}
