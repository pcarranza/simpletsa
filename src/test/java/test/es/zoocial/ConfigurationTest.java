package test.es.zoocial;

import java.net.URL;
import java.util.Map;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import es.zoocial.tsa.Configuration;

public class ConfigurationTest {

	@Test
	public void testCreateConfiguration() {
		Assert.assertNotNull("Could not create configuration cfg instance", new Configuration());
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testLoadConfigurationWithoutEnvVarThrowsIllegalArgumentException() {
		Configuration cfg = new Configuration();
		{
			cfg = Mockito.spy(cfg);
			Mockito.doReturn("")
					.when(cfg)
					.getConfigurationFilename();
		}
		cfg.loadConfiguration();
	}

	
	@Test(expected=IllegalArgumentException.class)
	public void testLoadConfigurationWithInvalidEnvVarThrowsIllegalArgumentException() {
		Configuration cfg = new Configuration();
		{
			cfg = Mockito.spy(cfg);
			Mockito.doReturn("file://invalidfile-" + UUID.randomUUID().toString())
					.when(cfg)
					.getConfigurationFilename();
		}
		cfg.loadConfiguration();
	}
	
	@Test
	public void testLoadConfigurationWithValidFileURL() {
		Configuration cfg = new Configuration();
		{
			URL confFileUrl = getClass().getResource(getClass().getSimpleName() + ".properties");
			cfg = Mockito.spy(cfg);
			Mockito.doReturn(confFileUrl.toString())
					.when(cfg)
					.getConfigurationFilename();
		}
		cfg.loadConfiguration();
		
		Assert.assertEquals("Configuration test key", "has some value", cfg.getProperty("some-key"));
	}

	
	@Test
	public void testLoadConfigurationWithValidFileURLAsArgument() {
		Configuration cfg = new Configuration();
		URL confFileUrl = getClass().getResource(getClass().getSimpleName() + ".properties");
		cfg.loadConfiguration(confFileUrl.toString());
		
		Assert.assertEquals("Configuration test key", "has some value", cfg.getProperty("some-key"));
	}
	
	@Test
	public void testGetConfigurationPropertySet() {
		Configuration cfg = new Configuration();
		URL confFileUrl = getClass().getResource(getClass().getSimpleName() + ".properties");
		cfg.loadConfiguration(confFileUrl.toString());
		
		Map<String, String> propertySet = cfg.getPropertySet("myprefix.");
		Assert.assertNotNull("Property Set", propertySet);
		Assert.assertEquals("Value 1", propertySet.get("value1"), "value 1");
		Assert.assertEquals("Value 2", propertySet.get("value2"), "value 2");
	}

}
