package com.luckia.biller.core.serialization.entities;

import java.lang.reflect.Type;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.luckia.biller.core.model.Bill;

public class BillSerializer extends AbstractBillSerializer implements JsonSerializer<Bill> {

	@Override
	public JsonElement serialize(Bill src, Type typeOfSrc, JsonSerializationContext context) {
		JsonObject bill = serialize(src, context);

		JsonObject liquidation = new JsonObject();
		liquidation.addProperty("id", src.getLiquidation().getId());
		bill.add("liquidation", liquidation);

		bill.add("billType", context.serialize(src.getBillType()));
		bill.add("billDetails", context.serialize(src.getBillDetails()));
		bill.add("liquidationDetails", context.serialize(src.getLiquidationDetails()));
		return bill;
	}
}
