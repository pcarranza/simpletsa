package es.zoocial.tsa;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.util.Map;
import java.util.Set;

import es.zoocial.util.ArgsHelper;
import es.zoocial.util.IOHelper;
import es.zoocial.util.StringHelper;

public class KeystoreHandler {

	private KeyStore keystore = null;
	private KeystoreModel model = null;

	public void loadKeystore(KeystoreModel model) {
		ArgsHelper.notNull("Keystore model", model);
		this.model = model;

		File keystoreFile = IOHelper.getValidFile(model.getKeystoreFilename());

		try {
			this.keystore = KeyStore.getInstance(model.getKeystoreType());
		} catch (KeyStoreException e) {
			throw new IllegalArgumentException(String.format("Invalid keystoretype %s", model.getKeystoreType()), e);
		}

		FileInputStream is = null;
		try {
			is = new FileInputStream(keystoreFile);
			this.keystore.load(is, model.getKeystorePassword().toCharArray());
			testKeystore();
			testKeystore(); // Second time to check that cert alias is a forward only integer
		} catch (FileNotFoundException e) {
			throw new IllegalArgumentException(String.format("File %s could not be found", model.getKeystoreFilename()), e);
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalArgumentException(String.format("Could not load keystore %s", model.getKeystoreFilename()), e);
		} catch (CertificateException e) {
			throw new IllegalArgumentException(String.format("Could not load keystore %s", model.getKeystoreFilename()), e);
		} catch (IOException e) {
			throw new IllegalArgumentException(String.format("Could not load keystore %s", model.getKeystoreFilename()), e);
		} catch (UnrecoverableKeyException e) {
			throw new IllegalArgumentException(String.format("Keystore %s validation tests failed", model.getKeystoreFilename()), e);
		} catch (KeyStoreException e) {
			throw new IllegalArgumentException(String.format("Keystore %s validation tests failed", model.getKeystoreFilename()), e);
		} finally {
			IOHelper.closeQuietly(is);
		}
	}

	
	public void testKeystore() throws KeyStoreException, UnrecoverableKeyException, NoSuchAlgorithmException, CertificateParsingException {
		ArgsHelper.notNull("keystore", keystore);
		Certificate[] certificateChain = keystore.getCertificateChain(model.getCertAlias());
		ArgsHelper.notNull("Certificate chain", certificateChain);
		
		Key key = keystore.getKey(model.getKeyAlias(), model.getKeyPassword().toCharArray());
		ArgsHelper.notNull("Private key", key);
		
		X509Certificate certificate = (X509Certificate)certificateChain[0];
		Set<String> criticalExtensionOIDs = certificate.getCriticalExtensionOIDs();
		if (!criticalExtensionOIDs.contains("2.5.29.37"))
			throw new KeyStoreException("Certificate extended key usage is not marked as critical");
		
		boolean isForTimestamping = false;
		for (String keyUsage : certificate.getExtendedKeyUsage()) {
			if ("1.3.6.1.5.5.7.3.8".equals(keyUsage)) {
				isForTimestamping = true;
				break;
			}
		}
		if (!isForTimestamping) {
			throw new KeyStoreException("Certificate extended key usage is not for timestamping");
		}
	}
	
	
	public X509Certificate getCertificate() throws KeyStoreException {
		ArgsHelper.notNull("Keystore", keystore);
		ArgsHelper.notNull("Model", model);
		return (X509Certificate)keystore.getCertificate(model.getCertAlias());
		
	}
	
	
	public Certificate[] getCertChain() throws KeyStoreException {
		ArgsHelper.notNull("Keystore", keystore);
		ArgsHelper.notNull("Model", model);
		return keystore.getCertificateChain(model.getCertAlias());
	}
	
	
	
	public PrivateKey getPrivateKey() throws UnrecoverableKeyException, KeyStoreException, NoSuchAlgorithmException {
		ArgsHelper.notNull("Keystore", keystore);
		ArgsHelper.notNull("Model", model);
		return (PrivateKey)keystore.getKey(model.keyAlias, model.keyPassword.toCharArray());
	}
	
	
	public PublicKey getPublicKey() throws KeyStoreException {
		return getCertificate().getPublicKey();
	}
	

	public static class KeystoreModel {

		private String keystoreType;

		private String keystoreFilename;

		private String keystorePassword;

		private String keyPassword;

		private String keyAlias;

		private String certAlias;

		public static KeystoreModel keystoreType(String keystoreType) {
			KeystoreModel model = new KeystoreModel();
			model.keystoreType = keystoreType;
			return model;
		}

		public static KeystoreModel pkcs12() {
			return keystoreType("PKCS12");
		}

		public static KeystoreModel jks() {
			return keystoreType("JKS");
		}

		public static KeystoreModel fromMap(Map<String, String> keystoreMap) {
			ArgsHelper.notNull("Keystore Configuration Map", keystoreMap);
			KeystoreModel model = keystoreType(keystoreMap.get("keystoretype"));
			model.keystoreFilename(keystoreMap.get("keystorefile"));
			model.keystorePassword(keystoreMap.get("keystorepassword"));
			model.keyPassword(keystoreMap.get("keypassword"));
			model.keyAlias(keystoreMap.get("keyalias"));
			model.certAlias(keystoreMap.get("certalias"));
			return model;

		}

		public KeystoreModel keystoreFilename(String file) {
			this.keystoreFilename = file;
			return this;
		}

		public KeystoreModel keyPassword(String password) {
			this.keyPassword = password;
			return this;
		}

		public KeystoreModel keystorePassword(String password) {
			this.keystorePassword = password;
			return this;
		}

		public KeystoreModel keyAlias(String alias) {
			this.keyAlias = alias;
			return this;
		}

		public KeystoreModel certAlias(String alias) {
			this.certAlias = alias;
			return this;
		}

		public String getKeystoreType() {
			return StringHelper.notEmpty(keystoreType, "JKS");
		}

		public String getKeystoreFilename() {
			return keystoreFilename;
		}

		public String getKeystorePassword() {
			return StringHelper.voidIfNull(keystorePassword);
		}

		public String getKeyPassword() {
			return StringHelper.voidIfNull(keyPassword);
		}

		public String getKeyAlias() {
			return StringHelper.notEmpty(keyAlias, "mykey");
		}

		public String getCertAlias() {
			return StringHelper.notEmpty(certAlias, "mykey");
		}

	}

}