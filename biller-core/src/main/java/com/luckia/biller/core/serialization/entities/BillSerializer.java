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

		bill.addProperty("netAmount", src.getNetAmount());
		bill.addProperty("vatAmount", src.getVatAmount());
		bill.addProperty("vatPercent", src.getVatPercent());
		bill.addProperty("liquidationTotalAmount", src.getLiquidationTotalAmount());
		bill.addProperty("liquidationTotalVat", src.getLiquidationTotalVat());
		bill.addProperty("liquidationTotalNetAmount", src.getLiquidationTotalNetAmount());
		bill.addProperty("liquidationBetAmount", src.getLiquidationBetAmount());
		bill.addProperty("liquidationSatAmount", src.getLiquidationSatAmount());
		bill.addProperty("liquidationPricePerLocation", src.getLiquidationPricePerLocation());
		bill.addProperty("liquidationManualAmount", src.getLiquidationManualAmount());
		bill.addProperty("liquidationOuterAmount", src.getLiquidationOuterAmount());

		bill.add("billType", context.serialize(src.getBillType()));
		bill.add("billDetails", context.serialize(src.getBillDetails()));
		bill.add("liquidationDetails", context.serialize(src.getLiquidationDetails()));
		bill.add("billRawData", context.serialize(src.getBillRawData()));

		return bill;
	}
}
