package com.luckia.biller.core.serialization.entities;

import java.lang.reflect.Type;

import org.apache.commons.lang3.StringUtils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.luckia.biller.core.model.Liquidation;

public class LiquidationSerializer extends AbstractBillSerializer implements JsonSerializer<Liquidation> {

	@Override
	public JsonElement serialize(Liquidation src, Type typeOfSrc, JsonSerializationContext context) {
		JsonObject liquidation = serialize(src, context);
		if (src.getLiquidationResults() != null) {
			liquidation.add("liquidationResults", context.serialize(src.getLiquidationResults()));
		}
		if (src.getDetails() != null) {
			liquidation.add("details", context.serialize(src.getDetails()));
		}
		if (StringUtils.isNotBlank(src.getModelVersion())) {
			liquidation.addProperty("modelVersion", src.getModelVersion());
		}
		return liquidation;
	}

}
