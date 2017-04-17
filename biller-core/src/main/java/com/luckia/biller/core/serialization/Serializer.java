package com.luckia.biller.core.serialization;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.Date;

import javax.inject.Inject;

import org.apache.commons.io.IOUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.Injector;
import com.luckia.biller.core.common.BillerException;
import com.luckia.biller.core.model.AuditData;
import com.luckia.biller.core.model.Bill;
import com.luckia.biller.core.model.BillType;
import com.luckia.biller.core.model.Liquidation;
import com.luckia.biller.core.model.Owner;
import com.luckia.biller.core.model.TerminalRelation;
import com.luckia.biller.core.model.UserActivity;
import com.luckia.biller.core.serialization.entities.BillSerializer;
import com.luckia.biller.core.serialization.entities.BillTypeSerializer;
import com.luckia.biller.core.serialization.entities.LiquidationSerializer;
import com.luckia.biller.core.serialization.entities.OwnerSerializer;
import com.luckia.biller.core.serialization.entities.UserActivitySerializer;

import lombok.extern.slf4j.Slf4j;

/**
 * Componente encargado de serializar y deserializar las entidades utilizando JSON.
 */
@Slf4j
public class Serializer {

	private static final String UTF_8 = "UTF-8";

	private final Injector injector;

	@Inject
	public Serializer(Injector injector) {
		this.injector = injector;
	}

	public <T> GsonBuilder getBuilder() {
		GsonBuilder builder = new GsonBuilder();
		builder.setPrettyPrinting();
		builder.addSerializationExclusionStrategy(new SerializationExclusionStrategy());
		builder.registerTypeAdapter(Date.class,
				injector.getInstance(DateSerializer.class));
		builder.registerTypeAdapter(BigDecimal.class,
				injector.getInstance(BigDecimalSerializer.class));
		// Entity custom serialization
		builder.registerTypeHierarchyAdapter(Owner.class,
				injector.getInstance(OwnerSerializer.class));
		builder.registerTypeHierarchyAdapter(TerminalRelation.class,
				injector.getInstance(TerminalRelationSerializer.class));
		builder.registerTypeAdapter(BillType.class,
				injector.getInstance(BillTypeSerializer.class));
		builder.registerTypeHierarchyAdapter(Bill.class,
				injector.getInstance(BillSerializer.class));
		builder.registerTypeHierarchyAdapter(Liquidation.class,
				injector.getInstance(LiquidationSerializer.class));
		builder.registerTypeHierarchyAdapter(AuditData.class,
				injector.getInstance(AuditDataSerializer.class));
		builder.registerTypeHierarchyAdapter(UserActivity.class,
				injector.getInstance(UserActivitySerializer.class));
		return builder;
	}

	public void serialize(Object object, OutputStream entityStream) {
		try {
			Gson gson = getBuilder().create();
			String json = gson.toJson(object);
			entityStream.write(json.getBytes("UTF8"));
		}
		catch (Exception ex) {
			log.error("Serialization error: {}", ex.getMessage());
			throw new BillerException(ex);
		}
	}

	public String toJson(Object object) {
		try {
			return getBuilder().create().toJson(object);
		}
		catch (Exception ex) {
			log.error("Serialization error: {}", ex.getMessage());
			throw new BillerException(ex);
		}
	}

	public <T> T deserialize(Class<T> type, InputStream entityStream) {
		InputStreamReader streamReader = null;
		try {
			streamReader = new InputStreamReader(entityStream, UTF_8);
			Type jsonType = type;
			return getBuilder().create().fromJson(streamReader, jsonType);
		}
		catch (IOException ex) {
			throw new BillerException(ex);
		}
		finally {
			IOUtils.closeQuietly(streamReader);
		}
	}
}
