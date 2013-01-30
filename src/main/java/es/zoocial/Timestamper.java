package es.zoocial;

import java.security.Security;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.tsp.TimeStampRequest;
import org.bouncycastle.tsp.TimeStampResponse;

public class Timestamper {
	
	static {
		Security.addProvider(new BouncyCastleProvider());
	}
	
	
	public TimeStampResponse getTimestamp(TimeStampRequest timeStampRequest) {
		return null;
	}

}
