package test.es.zoocial;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class DigestHelper {
	
	public static byte[] digest(String message) {
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
