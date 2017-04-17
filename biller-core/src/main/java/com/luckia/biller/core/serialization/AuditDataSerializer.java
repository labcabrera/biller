package com.luckia.biller.core.serialization;

import java.lang.reflect.Type;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.luckia.biller.core.model.AuditData;

public class AuditDataSerializer implements JsonSerializer<AuditData> {

	@Override
	public JsonElement serialize(AuditData src, Type typeOfSrc,
			JsonSerializationContext context) {
		JsonObject json = new JsonObject();
		json.add("created", context.serialize(src.getCreated()));
		json.add("modified", context.serialize(src.getModified()));
		json.add("deleted", context.serialize(src.getDeleted()));
		if (src.getModifiedBy() != null) {
			JsonObject user = new JsonObject();
			user.addProperty("id", src.getModifiedBy().getId());
			user.addProperty("name", src.getModifiedBy().getName());
			json.add("modifiedBy", user);
		}
		return json;
	}

}
