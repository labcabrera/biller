package com.luckia.biller.core.common;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.luckia.biller.core.Constants;

/**
 * Componente encargado de leer la configuracion de la aplicacion.<br>
 * La aplicacion se gestiona a partir de un fichero *.conf estructurado en secciones con la siguiente estructura:
 * 
 * <pre>
 * [dummy category 1]
 * user=name
 * someAttribute=test
 * 
 * [dummy category 2]
 * somePath=/home/ogp/dummy
 * version=1.1.2
 * ...
 * </pre>
 * 
 */
@Singleton
public class SettingsManager {

	public static final String SYSTEM_PROPERTY_KEY_CONFIG_FILE = "ogp-common.resource.path";

	private static final Logger LOG = LoggerFactory.getLogger(SettingsManager.class);
	private static final String CONFIG_FILE_CHARSET = "UTF8";
	private static final String PATTERN_SECTION = "\\[(.+)\\]";

	private Map<String, Properties> sections = null;

	public Properties getProperties(String sectionName) {
		if (sections == null) {
			throw new RuntimeException("Service is not initialized. Call method load before");
		} else if (!sections.containsKey(sectionName)) {
			throw new RuntimeException(String.format("Missing section name %s", sectionName));
		} else {
			return sections.get(sectionName);
		}
	}

	public String getProperty(String sectionName, String property) {
		Properties properties = getProperties(sectionName);
		if (!properties.containsKey(property)) {
			throw new RuntimeException(String.format("Missing property %s in section %s", property, sectionName));
		}
		return properties.getProperty(property);
	}

	@SuppressWarnings("unchecked")
	public <T> T getProperty(String sectionName, String property, Class<T> typeClass) {
		String stringValue = getProperty(sectionName, property);
		if (String.class.isAssignableFrom(typeClass)) {
			return (T) stringValue;
		} else if (Integer.class.isAssignableFrom(typeClass)) {
			return (T) new Integer(stringValue);
		} else {
			throw new InvalidParameterException(String.format("Class %s is not a valid type", typeClass.getName()));
		}
	}

	public SettingsManager load() {
		String home = System.getProperty("user.home");
		File config = new File(home, ".biller.config");
		if (config.exists()) {
			try {
				FileInputStream in;
				in = new FileInputStream(config);
				load(in);
			} catch (Exception ex) {
				throw new RuntimeException("Error loading application config", ex);
			}
		} else {
			throw new RuntimeException("Missing application config " + config.getAbsolutePath());
		}
		return this;
	}

	public SettingsManager load(String classPathResource) {
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		InputStream configInputStream = classLoader.getResourceAsStream(classPathResource);
		Validate.notNull(configInputStream, String.format("Missing configuration file '%s' in classpath", Constants.APP_CONFIG_FILE));
		return load(configInputStream);
	}

	public SettingsManager load(InputStream inputStream) {
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, Charset.forName(CONFIG_FILE_CHARSET)));
		try {
			sections = new HashMap<String, Properties>();
			Pattern patternSection = Pattern.compile(PATTERN_SECTION);
			// Pattern patternKeyValue = Pattern.compile(PATTERN_KEY_VALUE);
			String currentSection = StringUtils.EMPTY;
			String line;
			while ((line = reader.readLine()) != null) {
				if (!StringUtils.isBlank(line) && !StringUtils.trim(line).startsWith("#") && !StringUtils.trim(line).startsWith("//")) {
					Matcher matcherSection = patternSection.matcher(line);
					// Matcher matcherKeyValue = patternKeyValue.matcher(line);
					if (matcherSection.matches()) {
						currentSection = matcherSection.group(1);
						sections.put(currentSection, new Properties());
						LOG.trace(String.format("Starting section %s", currentSection));
					} else {
						int index = line.indexOf("=");
						String key = line.substring(0, index);
						String value = line.substring(index + 1, line.length());
						sections.get(currentSection).put(key, value);
						LOG.trace(String.format("Readed '%s' = '%s'", key, value));
					}
				}
			}
			return this;
		} catch (IOException ex) {
			throw new RuntimeException("Error reading configuration", ex);
		} finally {
			try {
				reader.close();
			} catch (Exception ignore) {
			}
		}
	}
}
