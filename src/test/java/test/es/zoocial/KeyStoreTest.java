package test.es.zoocial;

import java.util.Map;

import org.junit.Test;

import es.zoocial.Configuration;
import es.zoocial.KeystoreHandler;
import es.zoocial.KeystoreHandler.KeystoreModel;

public class KeyStoreTest {

	// keytool -genkeypair -alias keypair -keysize 1024 -validity 3650 -keypass password -keystore test.jks -storepass password -storetype jks -v
	
	@Test
	public void testLoadKeystore() {
		Configuration conf = new Configuration();
		conf.loadConfiguration(getClass().getResource(getClass().getSimpleName() + ".properties").toString());
		Map<String, String> map = conf.getPropertySet("test-keystore.");
		map.put("keystorefile", getClass().getResource("test.jks").toString());
		KeystoreModel model = KeystoreHandler.KeystoreModel.fromMap(map);
		KeystoreHandler keystore = new KeystoreHandler();
		keystore.loadKeystore(model);
	}
	
}
