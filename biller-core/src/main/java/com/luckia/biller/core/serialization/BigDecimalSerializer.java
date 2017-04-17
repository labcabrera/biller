package com.luckia.biller.core.serialization;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.text.NumberFormat;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class BigDecimalSerializer
		implements JsonSerializer<BigDecimal>, JsonDeserializer<BigDecimal> {

	@Override
	public JsonElement serialize(BigDecimal value, Type typeOfSrc,
			JsonSerializationContext context) {
		JsonElement result = null;
		if (value != null) {
			NumberFormat numberFormat = NumberFormat.getNumberInstance();
			numberFormat.setMinimumFractionDigits(2);
			numberFormat.setMaximumFractionDigits(2);
			numberFormat.setGroupingUsed(true);
			return new JsonPrimitive(numberFormat.format(value));
		}
		return result;
	}

	@Override
	public BigDecimal deserialize(JsonElement json, Type typeOfT,
			JsonDeserializationContext context) throws JsonParseException {
		return json != null ? new BigDecimal(json.getAsString().replaceAll(",", ""))
				: null;
	}
}
