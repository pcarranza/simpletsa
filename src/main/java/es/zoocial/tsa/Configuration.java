package es.zoocial.tsa;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import es.zoocial.util.ArgsHelper;
import es.zoocial.util.IOHelper;
import es.zoocial.util.StringHelper;

public class Configuration {
	
	private final Properties props;
	
	public Configuration() {
		props = new Properties();
	}

	public String getConfigurationFilename() {
		String configurationFilename = System.getenv("CFG_FILE");
		if (StringHelper.isEmpty(configurationFilename)) {
			throw new IllegalArgumentException("Missing environment variable CFG_FILE");
		}
		return configurationFilename;
	}
	
	/**
	 * Tries to load configuration using an environment variable CFG_FILE as a conf source
	 */
	public void loadConfiguration() {
		loadConfiguration(getConfigurationFilename());
	}

	
	public void loadConfiguration(String configuration) {
		ArgsHelper.notEmpty("configuration", configuration);
		
		File config = IOHelper.getValidFile(configuration);
		
		InputStream is = null;
		try {
			is = new FileInputStream(config);
			props.load(is);
			
		} catch (IOException e) {
			throw new IllegalArgumentException(String.format("Could not load configuration from file %s", config.getAbsolutePath()), e);
		} finally {
			IOHelper.closeQuietly(is);
		}
	}
	
	
	public String getProperty(String key) {
		return props.getProperty(key);
	}
	
	
	public Map<String, String> getPropertySet(String prefix) {
		Iterator<String> keyIterator = props.stringPropertyNames().iterator();
		HashMap<String, String> map = new HashMap<String, String>();
		while (keyIterator.hasNext()) {
			String key = keyIterator.next();
			if (StringHelper.startsWith(prefix, key)) {
				String newKey = key.substring(prefix.length());
				while (newKey.startsWith("."))
					newKey = newKey.substring(1);
				map.put(newKey, getProperty(key));
			}
		}
		return map;
	}

}
