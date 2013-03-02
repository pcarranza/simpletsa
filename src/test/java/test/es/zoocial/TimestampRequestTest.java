package test.es.zoocial;

import org.bouncycastle.tsp.TSPAlgorithms;
import org.bouncycastle.tsp.TSPException;
import org.bouncycastle.tsp.TimeStampRequest;
import org.bouncycastle.tsp.TimeStampRequestGenerator;
import org.bouncycastle.tsp.TimeStampResponse;
import org.junit.Assert;
import org.junit.Test;

import es.zoocial.KeystoreHandler;
import es.zoocial.Timestamper;

public class TimestampRequestTest {
	
	@Test
	public void testTimestamp() throws TSPException {
		KeystoreHandler keystore = KeystoreHelper.getP12Keystore();
		
		TimeStampRequestGenerator generator = new TimeStampRequestGenerator();
		TimeStampRequest timeStampRequest = generator.generate(
				TSPAlgorithms.SHA1, DigestHelper.digest("this is a test message"));
		
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
				TSPAlgorithms.SHA1, DigestHelper.digest("this is a test message"));

		TimeStampRequest timeStampRequest2 = generator.generate(
				TSPAlgorithms.SHA1, DigestHelper.digest("this is a another test message"));

		TimeStampResponse response = null;
		response = new Timestamper(keystore).timestamp(timeStampRequest);
		Assert.assertNotNull("Timestamp Response should not be null", response);
		Assert.assertNotNull("Timestamp token should not be null", response.getTimeStampToken());
		
		response.validate(timeStampRequest);
		response.validate(timeStampRequest2);
	}
	


}
