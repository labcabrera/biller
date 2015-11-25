package com.luckia.biller.core.serialization.entities;

import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.luckia.biller.core.model.AbstractBill;

public abstract class AbstractBillSerializer extends AbstractEntitySerializer {

	public JsonObject serialize(AbstractBill src, JsonSerializationContext context) {
		JsonObject result = new JsonObject();
		result.addProperty("id", src.getId());
		result.addProperty("code", src.getCode());
		result.add("billDate", context.serialize(src.getBillDate()));
		result.add("dateFrom", context.serialize(src.getDateFrom()));
		result.add("dateTo", context.serialize(src.getDateTo()));
		result.addProperty("amount", src.getAmount());
		result.addProperty("comments", src.getComments());
		result.addProperty("commentsPdf", src.getCommentsPdf());

		if (src.getSender() != null) {
			JsonObject sender = new JsonObject();
			sender.addProperty("id", src.getSender().getId());
			sender.addProperty("name", src.getSender().getName());
			result.add("sender", sender);
		}

		if (src.getReceiver() != null) {
			JsonObject receiver = new JsonObject();
			receiver.addProperty("id", src.getSender().getId());
			receiver.addProperty("name", src.getSender().getName());
			result.add("receiver", receiver);
		}

		if (src.getModel() != null) {
			JsonObject model = new JsonObject();
			model.addProperty("id", src.getModel().getId());
			model.addProperty("name", src.getModel().getName());
			result.add("model", model);
		}

		if (src.getCurrentState() != null) {
			result.add("currentState", context.serialize(src.getCurrentState()));
		}
		if (src.getPdfFile() != null) {
			result.add("pdfFile", context.serialize(src.getPdfFile()));
		}
		if (src.getAuditData() != null) {
			result.add("auditData", context.serialize(src.getAuditData()));
		}
		return result;
	}

}
