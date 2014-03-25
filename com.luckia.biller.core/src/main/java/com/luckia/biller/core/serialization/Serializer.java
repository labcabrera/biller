package com.luckia.biller.core.serialization;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.math.BigDecimal;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.luckia.biller.core.model.BillType;
import com.luckia.biller.core.model.Owner;

public class Serializer {

	private static final String UTF_8 = "UTF-8";

	public <T> GsonBuilder getBuilder() {
		GsonBuilder builder = new GsonBuilder();
		builder.setPrettyPrinting();
		builder.addSerializationExclusionStrategy(new SerializationExclusionStrategy());
		builder.setDateFormat("yyyy-MM-dd'T'HH:mm:ss.sss'Z'");
		builder.registerTypeHierarchyAdapter(Owner.class, new OwnerSerializer());
		builder.registerTypeAdapter(BigDecimal.class, new BigDecimalSerializer());
		builder.registerTypeAdapter(BillType.class, new BillTypeSerializer());
		return builder;
	}

	public void serialize(Object object, OutputStream entityStream) {
		try {
			Gson gson = getBuilder().create();
			String json = gson.toJson(object);
			entityStream.write(json.getBytes("UTF8"));
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	public <T> T deserialize(Class<T> type, InputStream entityStream) {
		InputStreamReader streamReader = null;
		try {
			streamReader = new InputStreamReader(entityStream, UTF_8);
			Type jsonType = type;
			return getBuilder().create().fromJson(streamReader, jsonType);
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		} finally {
			if (streamReader != null) {
				try {
					streamReader.close();
				} catch (IOException ignore) {
				}
			}
		}
	}
}
