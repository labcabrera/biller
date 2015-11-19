package com.luckia.biller.core.serialization.entities;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.luckia.biller.core.model.BillType;

/**
 * En la serializacion del tipo enumerado incluimos la descripción de este. Al deserializar el enumerado ignoramos esta descripción.
 */
public class BillTypeSerializer implements JsonSerializer<BillType>, JsonDeserializer<BillType> {

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

	@Override
	public BillType deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
		BillType result = null;
		if (json != null) {
			if (json.isJsonObject() && json.getAsJsonObject().get("value") != null) {
				result = BillType.valueOf(json.getAsJsonObject().get("value").getAsString());
			}
		}
		return result;
	}

}
