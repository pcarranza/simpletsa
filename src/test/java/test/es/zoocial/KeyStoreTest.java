package test.es.zoocial;

import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;

import org.junit.Test;

import es.zoocial.tsa.KeystoreHandler;

public class KeyStoreTest {
	
	@Test
	public void testGetCertificateChain() throws KeyStoreException {
		KeystoreHandler keystore = KeystoreHelper.getP12Keystore();
		org.junit.Assert.assertNotNull("Certificate chain", keystore.getCertChain());
	}

	
	@Test
	public void testGetCertificate() throws KeyStoreException {
		KeystoreHandler keystore = KeystoreHelper.getP12Keystore();
		org.junit.Assert.assertNotNull("Certificate", keystore.getCertificate());
	}

	
	@Test
	public void testGetPrivateKey() throws UnrecoverableKeyException, KeyStoreException, NoSuchAlgorithmException {
		KeystoreHandler keystore = KeystoreHelper.getP12Keystore();
		org.junit.Assert.assertNotNull("Private Key", keystore.getPrivateKey());
	}
	
	@Test
	public void testGetPublicKey() throws KeyStoreException {
		KeystoreHandler keystore = KeystoreHelper.getP12Keystore();
		org.junit.Assert.assertNotNull("Public Key", keystore.getPublicKey());
	}
	
}
