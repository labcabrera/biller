package com.luckia.biller.core.serialization.entities;

import java.lang.reflect.Type;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.luckia.biller.core.model.UserActivity;

public class UserActivitySerializer implements JsonSerializer<UserActivity> {

	@Override
	public JsonElement serialize(UserActivity src, Type typeOfSrc,
			JsonSerializationContext context) {
		JsonObject json = new JsonObject();
		json.addProperty("id", src.getId());
		json.addProperty("type", src.getType().name());
		json.add("date", context.serialize(src.getDate()));
		JsonObject user = new JsonObject();
		user.addProperty("id", src.getUser().getId());
		user.addProperty("name", src.getUser().getName());
		json.add("user", user);
		json.addProperty("data", src.getData());
		return json;
	}

}
