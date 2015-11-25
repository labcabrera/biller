package com.luckia.biller.core.serialization.entities;

import java.lang.reflect.Type;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.luckia.biller.core.model.Liquidation;

public class LiquidationSerializer extends AbstractBillSerializer implements JsonSerializer<Liquidation> {

	@Override
	public JsonElement serialize(Liquidation src, Type typeOfSrc, JsonSerializationContext context) {
		JsonObject liquidation = serialize(src, context);
		liquidation.add("liquidationResults", context.serialize(src.getLiquidationResults()));
		liquidation.add("details", context.serialize(src.getDetails()));
		liquidation.addProperty("modelVersion", src.getModelVersion());

		return liquidation;
	}

}
