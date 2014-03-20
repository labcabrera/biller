package com.luckia.biller.core.model;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.MapKeyColumn;
import javax.persistence.Table;

/**
 * Esta entidad define diferentes parametros de configuracion, tales como el porcentaje del IVA, la empresa emisora de las facturas o los
 * parametros del envio de correos.<br>
 * Al tener de momento pocos parametros los almacenamos en un unico registro. En caso de que crecieran seria mas conveniente guardarlos en
 * formato (clave-valor).
 * 
 */
@Entity
@Table(name = "S_APP_SETTINGS")
public class AppSettings {

	@Id
	@Column(name = "ID", length = 32)
	private String id;
	@ElementCollection
	@MapKeyColumn(name = "SETTINGS_KEY")
	@Column(name = "SETTINGS_VALUE")
	@CollectionTable(name = "S_APP_SETTINGS_VALUES", joinColumns = @JoinColumn(name = "SETTINGS_ID"))
	private Map<String, String> values;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	@SuppressWarnings("unchecked")
	public <T> T getValue(String key, Class<T> type) {
		Object result = null;
		String value = values.get(key);
		if (value != null) {
			if (type == String.class) {
				result = value;
			} else if (type == Long.class) {
				result = Long.parseLong(value);
			} else if (type == BigDecimal.class) {
				result = new BigDecimal(value);
			} else {
				throw new RuntimeException("Invalid type " + type);
			}
		}
		return (T) result;
	}

	public void setValue(String key, String value) {
		if (values == null) {
			values = new HashMap<String, String>();
		}
		values.put(key, value);
	}
}
