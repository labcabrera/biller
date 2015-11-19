package com.luckia.biller.core.serialization.entities;

import java.lang.reflect.Type;
import java.util.Arrays;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.luckia.biller.core.model.Liquidation;

public class LiquidationSerializer extends AbstractEntitySerializer implements JsonSerializer<Liquidation> {

	@Override
	public JsonElement serialize(Liquidation src, Type typeOfSrc, JsonSerializationContext context) {
		JsonObject result = context.serialize(src, InternalLiquidation.class).getAsJsonObject();
		clean(result, "sender", Arrays.asList("id", "name"));
		clean(result, "receiver", Arrays.asList("id", "name"));
		clean(result, "model", Arrays.asList("id", "name"));
		return result;
	}

	@SuppressWarnings("serial")
	private final class InternalLiquidation extends Liquidation {
	}

}
