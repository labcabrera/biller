package com.luckia.biller.core.serialization.entities;

import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public abstract class AbstractEntitySerializer {

	protected void clean(JsonObject object, String propertyName,
			List<String> availableProperties) {
		if (object.has(propertyName)) {
			JsonObject jsonObject = object.get(propertyName).getAsJsonObject();
			for (Iterator<Entry<String, JsonElement>> iterator = jsonObject.entrySet()
					.iterator(); iterator.hasNext();) {
				Entry<String, JsonElement> entrySet = iterator.next();
				String key = entrySet.getKey();
				if (!availableProperties.contains(key)) {
					iterator.remove();

				}
			}
		}

	}
}
