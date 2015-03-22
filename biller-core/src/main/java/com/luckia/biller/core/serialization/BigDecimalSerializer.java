package com.luckia.biller.core.serialization;

import java.lang.reflect.Type;
import java.math.BigDecimal;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class BigDecimalSerializer implements JsonSerializer<BigDecimal> {

	@Override
	public JsonElement serialize(BigDecimal value, Type typeOfSrc, JsonSerializationContext context) {
		JsonElement result = null;
		if (value != null) {
			result = new JsonPrimitive(value.toString());
		}
		return result;
	}

}
