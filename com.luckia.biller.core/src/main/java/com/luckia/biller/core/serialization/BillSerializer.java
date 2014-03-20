package com.luckia.biller.core.serialization;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Date;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.luckia.biller.core.model.Bill;

public class BillSerializer implements JsonSerializer<Bill> {

	@Override
	public JsonElement serialize(Bill obj, Type type, JsonSerializationContext ctx) {
		JsonObject from = new JsonObject();
		from.addProperty("id", ""); // obj.getFrom().getId());
		from.addProperty("name", ""); // obj.getFrom().getName());

		JsonObject to = new JsonObject();
		to.addProperty("id", ""); // obj.getTo().getId());
		to.addProperty("name", ""); // obj.getTo().getName());

		JsonObject result = new JsonObject();
		result.addProperty("id", ""); // obj.getId());
		result.add("from", from);
		result.add("to", to);
		result.addProperty("amount", formatCurrency(new BigDecimal("5435.23")));
		result.add("valueDate", ctx.serialize(new Date())); // obj.getValueDate()));
		result.addProperty("desc", ""); // obj.getDesc());
		result.addProperty("longDesc", ""); // obj.getLongDesc());
		return result;
	}

	private String formatCurrency(BigDecimal value) {
		NumberFormat nf = NumberFormat.getInstance();
		nf.setMinimumFractionDigits(2);
		nf.setMaximumFractionDigits(2);
		nf.setGroupingUsed(false);
		return nf.format(value);
	}

}
