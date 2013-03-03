package test.es.zoocial;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import es.zoocial.tsa.Configuration;
import es.zoocial.tsa.KeystoreHandler;
import es.zoocial.tsa.KeystoreHandler.KeystoreModel;

public class KeystoreHelper {
	
	private static KeystoreHandler keystore = null;
	
	public static KeystoreHandler getP12Keystore() {
		if (keystore == null) {
			Configuration conf = new Configuration();
			conf.loadConfiguration(KeystoreHelper.class.getResource("keystore.properties").toString());
			Map<String, String> propertySet = conf.getPropertySet("keystore");
			URL url = null;
			try {
				url = new URL("file:cert/tsa.p12");
			} catch (MalformedURLException e) {
				throw new RuntimeException(e);
			}
			propertySet.put("keystorefile", new File(url.getFile()).getAbsolutePath());
			KeystoreHandler handler = new KeystoreHandler();
			handler.loadKeystore(KeystoreModel.fromMap(propertySet));
			keystore = handler; 
		}
		return keystore;
	}
	
}
