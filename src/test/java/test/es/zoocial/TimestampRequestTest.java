package test.es.zoocial;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.bouncycastle.tsp.TSPException;
import org.bouncycastle.tsp.TimeStampRequest;
import org.bouncycastle.tsp.TimeStampRequestGenerator;
import org.bouncycastle.tsp.TimeStampResponse;
import org.junit.Assert;
import org.junit.Test;

import es.zoocial.KeystoreHandler;
import es.zoocial.Timestamper;

public class TimestampRequestTest {
	
	/*
		1.3.36.3.2.2 -> RIPEMD-128
		1.3.36.3.2.1 -> RIPEMD-160
		1.3.36.3.2.3 -> RIPEMD-256
		1.2.840.113549.2.5 -> MD5
		1.3.14.3.2.26 -> SHA-1
		2.16.840.1.101.3.4.2.4 -> SHA-224
		2.16.840.1.101.3.4.2.1 -> SHA-256
		2.16.840.1.101.3.4.2.2 -> SHA-384
		2.16.840.1.101.3.4.2.3 -> SHA-512
		1.2.643.2.2.9 -> GOST R 34.11-94
	 */
	
	@Test
	public void testTimestamp() throws TSPException {
		KeystoreHandler keystore = KeystoreHelper.getP12Keystore();
		
		TimeStampRequestGenerator generator = new TimeStampRequestGenerator();
		TimeStampRequest timeStampRequest = generator.generate(
				"1.3.14.3.2.26", digest("this is a test message")); // SHA1 with RSA
		
		TimeStampResponse response = null;
			response = new Timestamper(keystore).timestamp(timeStampRequest);
		Assert.assertNotNull("Timestamp Response should not be null", response);
		Assert.assertNotNull("Timestamp token should not be null", response.getTimeStampToken());
		
		response.validate(timeStampRequest);
	}

	
	@Test(expected=TSPException.class)
	public void testTimestampValidateWithOtherRequest() throws TSPException {
		KeystoreHandler keystore = KeystoreHelper.getP12Keystore();
		
		TimeStampRequestGenerator generator = new TimeStampRequestGenerator();
		TimeStampRequest timeStampRequest = generator.generate(
				"1.3.14.3.2.26", digest("this is a test message")); // SHA1 with RSA

		TimeStampRequest timeStampRequest2 = generator.generate(
				"1.3.14.3.2.26", digest("this is a another test message")); // SHA1 with RSA

		TimeStampResponse response = null;
		response = new Timestamper(keystore).timestamp(timeStampRequest);
		Assert.assertNotNull("Timestamp Response should not be null", response);
		Assert.assertNotNull("Timestamp token should not be null", response.getTimeStampToken());
		
		response.validate(timeStampRequest);
		response.validate(timeStampRequest2);
	}
	

	private byte[] digest(String message) {
		MessageDigest digester;
		try {
			digester = MessageDigest.getInstance("SHA1");
			digester.update(message.getBytes("UTF-8"));
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException("SHA1 not supported", e);
		} catch (UnsupportedEncodingException e) {
			throw new IllegalStateException("UTF-8 not supported", e);
		}
		
		return digester.digest();
	}

}
