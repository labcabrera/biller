package com.luckia.biller.core.serialization;

import java.lang.reflect.Type;

import org.apache.commons.lang.StringUtils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.luckia.biller.core.model.Owner;
import com.luckia.biller.core.model.Person;

/**
 * {@link JsonSerializer} que genera el nombre completo de la persona para evitar hacerlo desde el cliente.
 */
public class OwnerSerializer implements JsonSerializer<Owner> {

	@Override
	public JsonElement serialize(Owner owner, Type type, JsonSerializationContext ctx) {
		JsonObject result = ctx.serialize(owner, Person.class).getAsJsonObject();
		result.addProperty("completeName", getCompleteName(owner));
		return result;
	}

	private String getCompleteName(Owner owner) {
		StringBuffer sb = new StringBuffer(owner.getName());
		if (StringUtils.isNotBlank(owner.getFirstSurname())) {
			sb.append(" ").append(owner.getFirstSurname());
		}
		if (StringUtils.isNotBlank(owner.getSecondSurname())) {
			sb.append(" ").append(owner.getSecondSurname());
		}
		return sb.toString();
	}
}
