package es.zoocial;

import java.security.Security;
import java.util.Date;
import java.util.Hashtable;

import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.cms.AttributeTable;
import org.bouncycastle.cms.DefaultSignedAttributeTableGenerator;
import org.bouncycastle.cms.SignerInfoGenerator;
import org.bouncycastle.cms.SimpleAttributeTableGenerator;
import org.bouncycastle.cms.jcajce.JcaSignerInfoGeneratorBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;
import org.bouncycastle.tsp.TSPAlgorithms;
import org.bouncycastle.tsp.TimeStampRequest;
import org.bouncycastle.tsp.TimeStampResponse;
import org.bouncycastle.tsp.TimeStampResponseGenerator;
import org.bouncycastle.tsp.TimeStampTokenGenerator;

public class Timestamper {
	
	static {
		Security.addProvider(new BouncyCastleProvider());
	}

	private KeystoreHandler keystore;
	
	public Timestamper(KeystoreHandler handler) {
		this.keystore = handler;
	}
	
	public TimeStampResponse timestamp(TimeStampRequest timestampRequest)  {
		TimeStampTokenGenerator tokenGenerator = createTokenGenerator(timestampRequest);
		
		TimeStampResponseGenerator respGen = new TimeStampResponseGenerator(
				tokenGenerator, TSPAlgorithms.ALLOWED);
		
		Date tsDate = new Date();
		TimeStampResponse response;
		try {
			response = respGen.generate(timestampRequest, keystore.getCertificate().getSerialNumber(), tsDate);
		} catch (Exception e) {
			throw new RuntimeException("Could not generate timestamp response", e);
		}
		
		return response;
	}
	
	private TimeStampTokenGenerator createTokenGenerator(TimeStampRequest request) {
        JcaSignerInfoGeneratorBuilder sigBuilder;
		try {
			sigBuilder = new JcaSignerInfoGeneratorBuilder(
					new JcaDigestCalculatorProviderBuilder().setProvider("BC").build());
		} catch (OperatorCreationException e) {
			throw new RuntimeException("Could not create signature info generator", e);
		}

        AttributeTable attributes = new AttributeTable(new Hashtable<String, String>());
        sigBuilder.setSignedAttributeGenerator(
        		new DefaultSignedAttributeTableGenerator(attributes));
        sigBuilder.setUnsignedAttributeGenerator(new SimpleAttributeTableGenerator(attributes));

        SignerInfoGenerator signerInfoGen;
		try {
			signerInfoGen = sigBuilder.build(new JcaContentSignerBuilder("SHA1withRSA")
					.setProvider("BC")
					.build(keystore.getPrivateKey()), 
				keystore.getCertificate());
		} catch (Exception e) {
			throw new RuntimeException("Could not create signer info generator", e);
		}
        try {
			return new TimeStampTokenGenerator(signerInfoGen, new ASN1ObjectIdentifier("1.2.3"));
		} catch (Exception e) {
			throw new RuntimeException("Could not create timestamp token generator", e);
		}
	}

	
}
