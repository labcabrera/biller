package com.luckia.biller.core.serialization;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class DateSerializer implements JsonSerializer<Date>, JsonDeserializer<Date> {

	private static final String DATE_FORMAT_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.sss'Z'";

	/**
	 * Hay que tener en cuenta que las fechas vienen en UTC desde el front.
	 * 
	 * @param json
	 * @param typeOfT
	 * @param context
	 * @return
	 * @throws JsonParseException
	 */
	@Override
	public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
		try {
			String str = json.getAsString();
			SimpleDateFormat df = new SimpleDateFormat(DATE_FORMAT_PATTERN);
			df.setTimeZone(TimeZone.getTimeZone("UTC"));
			Date date = df.parse(str);
			return date;
		} catch (ParseException e) {
			throw new RuntimeException("Unparseable date: \"" + json + "\"");
		}
	}

	@Override
	public JsonElement serialize(Date src, Type typeOfSrc, JsonSerializationContext context) {
		SimpleDateFormat df = new SimpleDateFormat(DATE_FORMAT_PATTERN);
		String str = df.format(src);
		return new JsonPrimitive(str);
	}
}
