package com.luckia.biller.core.serialization.entities;

import java.lang.reflect.Type;
import java.util.Arrays;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.luckia.biller.core.model.Bill;

public class BillSerializer extends AbstractEntitySerializer implements JsonSerializer<Bill> {

	@Override
	public JsonElement serialize(Bill src, Type typeOfSrc, JsonSerializationContext context) {
		JsonObject result = context.serialize(src, InternalBill.class).getAsJsonObject();
		clean(result, "sender", Arrays.asList("id", "name"));
		clean(result, "receiver", Arrays.asList("id", "name"));
		clean(result, "liquidation", Arrays.asList("id"));
		clean(result, "model", Arrays.asList("id", "name"));
		return result;
	}

	@SuppressWarnings("serial")
	private final class InternalBill extends Bill {
	}

}
