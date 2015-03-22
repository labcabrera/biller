package com.luckia.biller.core.serialization;

import java.lang.reflect.Type;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.luckia.biller.core.model.TerminalRelation;

public class TerminalRelationSerializer implements JsonSerializer<TerminalRelation> {

	@Override
	public JsonElement serialize(TerminalRelation terminal, Type type, JsonSerializationContext ctx) {
		JsonObject result = new JsonObject();
		result.addProperty("id", terminal.getId());
		result.addProperty("code", terminal.getCode());
		result.addProperty("isMaster", terminal.getIsMaster());
		result.addProperty("comments", terminal.getComments());
		if (terminal.getStore() != null) {
			JsonObject store = new JsonObject();
			store.addProperty("id", terminal.getStore().getId());
			store.addProperty("name", terminal.getStore().getName());
			result.add("store", store);
		}
		if (terminal.getAuditData() != null) {
			result.add("auditData", ctx.serialize(terminal.getAuditData()));
		}
		return result;
	}

}
