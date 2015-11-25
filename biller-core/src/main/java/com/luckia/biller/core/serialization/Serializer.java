package com.luckia.biller.core.serialization;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.math.BigDecimal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import com.luckia.biller.core.model.Bill;
import com.luckia.biller.core.model.BillType;
import com.luckia.biller.core.model.Liquidation;
import com.luckia.biller.core.model.Owner;
import com.luckia.biller.core.model.TerminalRelation;
import com.luckia.biller.core.serialization.entities.BillSerializer;
import com.luckia.biller.core.serialization.entities.BillTypeSerializer;
import com.luckia.biller.core.serialization.entities.LiquidationSerializer;
import com.luckia.biller.core.serialization.entities.OwnerSerializer;

/**
 * Componente encargado de serializar y deserializar las entidades utilizando JSON.
 */
public class Serializer {

	private static final Logger LOG = LoggerFactory.getLogger(Serializer.class);

	private static final String UTF_8 = "UTF-8";
	private static final String DATE_FORMAT_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.sss'Z'";

	public <T> GsonBuilder getBuilder() {
		GsonBuilder builder = new GsonBuilder();
		builder.setPrettyPrinting();
		builder.addSerializationExclusionStrategy(new SerializationExclusionStrategy());
		builder.setDateFormat(DATE_FORMAT_PATTERN);
		builder.registerTypeHierarchyAdapter(Owner.class, new OwnerSerializer());
		builder.registerTypeHierarchyAdapter(TerminalRelation.class, new TerminalRelationSerializer());
		builder.registerTypeAdapter(BigDecimal.class, new BigDecimalSerializer());
		builder.registerTypeAdapter(BillType.class, new BillTypeSerializer());
		builder.registerTypeHierarchyAdapter(Bill.class, new BillSerializer());
		builder.registerTypeHierarchyAdapter(Liquidation.class, new LiquidationSerializer());
		return builder;
	}

	public void serialize(Object object, OutputStream entityStream) {
		try {
			Gson gson = getBuilder().create();
			String json = gson.toJson(object);
			entityStream.write(json.getBytes("UTF8"));
		} catch (Exception ex) {
			LOG.error("Serialization error: {}", ex.getMessage());
			throw new RuntimeException(ex);
		}
	}

	public String toJson(Object object) {
		try {
			return getBuilder().create().toJson(object);
		} catch (Exception ex) {
			LOG.error("Serialization error: {}", ex.getMessage());
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
