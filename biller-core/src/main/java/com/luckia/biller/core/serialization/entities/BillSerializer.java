package com.luckia.biller.core.serialization.entities;

import java.lang.reflect.Type;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.luckia.biller.core.model.Bill;

public class BillSerializer extends AbstractBillSerializer implements JsonSerializer<Bill> {

	private static final Logger LOG = LoggerFactory.getLogger(BillSerializer.class);

	@Override
	public JsonElement serialize(Bill src, Type typeOfSrc, JsonSerializationContext context) {
		try {
			JsonObject bill = serialize(src, context);

			if (src.getLiquidation() != null) {
				JsonObject liquidation = new JsonObject();
				liquidation.addProperty("id", src.getLiquidation().getId());
				bill.add("liquidation", liquidation);
			}

			bill.add("storeCash", context.serialize(src.getStoreCash()));
			bill.add("netAmount", context.serialize(src.getNetAmount()));
			bill.add("vatAmount", context.serialize(src.getVatAmount()));
			bill.add("vatPercent", context.serialize(src.getVatPercent()));
			bill.add("liquidationTotalAmount", context.serialize(src.getLiquidationTotalAmount()));
			bill.add("liquidationTotalVat", context.serialize(src.getLiquidationTotalVat()));
			bill.add("liquidationTotalNetAmount", context.serialize(src.getLiquidationTotalNetAmount()));
			bill.add("liquidationBetAmount", context.serialize(src.getLiquidationBetAmount()));
			bill.add("liquidationSatAmount", context.serialize(src.getLiquidationSatAmount()));
			bill.add("liquidationPricePerLocation", context.serialize(src.getLiquidationPricePerLocation()));
			bill.add("liquidationManualAmount", context.serialize(src.getLiquidationManualAmount()));
			bill.add("liquidationOuterAmount", context.serialize(src.getLiquidationOuterAmount()));

			bill.add("billType", context.serialize(src.getBillType()));
			if (src.getBillDetails() != null) {
				bill.add("billDetails", context.serialize(src.getBillDetails()));
			}
			if (src.getLiquidationDetails() != null) {
				bill.add("liquidationDetails", context.serialize(src.getLiquidationDetails()));
			}
			if (src.getBillRawData() != null) {
				bill.add("billRawData", context.serialize(src.getBillRawData()));
			}
			return bill;
		} catch (RuntimeException ex) {
			LOG.error("Bill serialization error: " + ex.getMessage());
			throw ex;
		}
	}
}
