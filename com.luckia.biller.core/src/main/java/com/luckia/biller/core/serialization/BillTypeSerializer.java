package com.luckia.biller.core.serialization;

import java.lang.reflect.Type;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.luckia.biller.core.model.BillType;

public class BillTypeSerializer implements JsonSerializer<BillType> {

	@Override
	public JsonElement serialize(BillType value, Type typeOfSrc, JsonSerializationContext context) {
		JsonElement result = null;
		if (value != null) {
			JsonObject obj = new JsonObject();
			obj.addProperty("value", value.name());
			obj.addProperty("desc", value.desc());
			result = obj;
		}
		return result;
	}

}
