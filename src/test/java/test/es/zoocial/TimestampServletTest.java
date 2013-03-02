package test.es.zoocial;

import java.io.IOException;
import java.net.URL;

import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.bouncycastle.tsp.TSPAlgorithms;
import org.bouncycastle.tsp.TSPException;
import org.bouncycastle.tsp.TimeStampRequest;
import org.bouncycastle.tsp.TimeStampRequestGenerator;
import org.bouncycastle.tsp.TimeStampResponse;
import org.bouncycastle.util.encoders.Base64;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.nio.SelectChannelConnector;
import org.mortbay.jetty.servlet.ServletHandler;

import es.zoocial.web.TimestampServlet;

public class TimestampServletTest {
	
	private static Server server;
	
	private String hostname = "127.0.0.1";
	private int port = 9000;
	private HttpClientBuilder builder = HttpClientBuilder.create();
	
	@BeforeClass
	public static void startServer() throws Exception {
		URL resource = TimestampServletTest.class.getResource("keystore.properties");
		System.setProperty("configuration", resource.toString());
		
		ServletHandler handler = new ServletHandler();
		handler.addServletWithMapping(TimestampServlet.class, "/tsa");
		
		
		SelectChannelConnector connector = new SelectChannelConnector();
		connector.setHost("127.0.0.1");
		connector.setPort(9000);
		
		server = new Server();
		server.addHandler(handler);
		server.addConnector(connector);
		server.start();
	}
	
	@Test
	public void testServletRequestGetMethodFails() throws IOException, TSPException {
		CloseableHttpClient client = builder.build();
		HttpHost host = new HttpHost(hostname, port);
		HttpGet get = new HttpGet("/tsa");
		
		CloseableHttpResponse response = null;
		response = client.execute(host, get);
		
		Assert.assertNotNull("response", response);
		Assert.assertEquals("Response code is not as expected", 
				HttpServletResponse.SC_METHOD_NOT_ALLOWED, response.getStatusLine().getStatusCode());
	}
	
	@Test
	public void testServletRequestWithEmptyRequestFailsWithBadRequest() throws IOException, TSPException {
		CloseableHttpClient client = builder.build();
		HttpHost host = new HttpHost(hostname, port);
		HttpPost post = new HttpPost("/tsa");
		
		ByteArrayEntity entity = new ByteArrayEntity(new byte[0]);
		
		post.setHeader("Content-Type", TimestampServlet.TIMESTAMP_QUERY_CONTENT_TYPE);
		post.setHeader(TimestampServlet.TRANSFER_ENCODING_HEADER, TimestampServlet.TRANSFER_ENCODING_BINARY);
		post.setEntity(entity);
		
		CloseableHttpResponse response = null;
		response = client.execute(host, post);
		Assert.assertNotNull("response", response);
		Assert.assertEquals("Response code is not as expected", 
				HttpServletResponse.SC_BAD_REQUEST, response.getStatusLine().getStatusCode());
	}
	
	@Test
	public void testServletRequestInBinaryModeWorks() throws IOException, TSPException {
		CloseableHttpClient client = builder.build();
		HttpHost host = new HttpHost(hostname, port);
		HttpPost post = new HttpPost("/tsa");
		
		TimeStampRequestGenerator generator = new TimeStampRequestGenerator();
		TimeStampRequest timeStampRequest = generator.generate(TSPAlgorithms.SHA1, DigestHelper.digest("this is a test message"));
		
		ByteArrayEntity entity = new ByteArrayEntity(timeStampRequest.getEncoded());
		
		post.setHeader("Content-Type", TimestampServlet.TIMESTAMP_QUERY_CONTENT_TYPE);
		post.setHeader(TimestampServlet.TRANSFER_ENCODING_HEADER, TimestampServlet.TRANSFER_ENCODING_BINARY);
		post.setEntity(entity);
		
		CloseableHttpResponse response = null;
		response = client.execute(host, post);
		Assert.assertNotNull("response", response);
		Assert.assertEquals("Response code is not as expected", 
				HttpServletResponse.SC_OK, response.getStatusLine().getStatusCode());
		
		HttpEntity responseEntity = response.getEntity();

		Assert.assertEquals("Response content type not as expected", 
				TimestampServlet.TIMESTAMP_REPLY_CONTENT_TYPE, responseEntity.getContentType().getValue());
		Assert.assertEquals("Response content transfer encoding not as expected", 
				TimestampServlet.TRANSFER_ENCODING_BINARY, 
				response.getFirstHeader(TimestampServlet.TRANSFER_ENCODING_HEADER).getValue());

		byte[] responseBytes = new byte[(int)responseEntity.getContentLength()];
		responseEntity.getContent().read(responseBytes);
		
		Assert.assertNotNull("Response content", responseBytes);
		TimeStampResponse tsresponse = new TimeStampResponse(responseBytes);
		tsresponse.validate(timeStampRequest);
	}
	

	@Test
	public void testServletRequestInBase64ModeWorks() throws IOException, TSPException {
		
		CloseableHttpClient client = builder.build();
		HttpHost host = new HttpHost(hostname, port);
		HttpPost post = new HttpPost("/tsa");
		
		TimeStampRequestGenerator generator = new TimeStampRequestGenerator();
		TimeStampRequest timeStampRequest = generator.generate(TSPAlgorithms.SHA1, DigestHelper.digest("this is a test message"));
		
		ByteArrayEntity entity = new ByteArrayEntity(Base64.encode(timeStampRequest.getEncoded()));
		
		post.setHeader("Content-Type", TimestampServlet.TIMESTAMP_QUERY_CONTENT_TYPE);
		post.setHeader(TimestampServlet.TRANSFER_ENCODING_HEADER, TimestampServlet.TRANSFER_ENCODING_BASE64);
		post.setEntity(entity);
		
		CloseableHttpResponse response = null;
		response = client.execute(host, post);
		Assert.assertNotNull("response", response);
		Assert.assertEquals("Response code is not as expected", 
				HttpServletResponse.SC_OK, response.getStatusLine().getStatusCode());

		HttpEntity responseEntity = response.getEntity();

		Assert.assertEquals("Response content type not as expected", 
				TimestampServlet.TIMESTAMP_REPLY_CONTENT_TYPE, responseEntity.getContentType().getValue());
		Assert.assertEquals("Response content transfer encoding not as expected", 
				TimestampServlet.TRANSFER_ENCODING_BASE64, 
				response.getFirstHeader(TimestampServlet.TRANSFER_ENCODING_HEADER).getValue());
		
		byte[] responseBytes = new byte[(int)responseEntity.getContentLength()];
		responseEntity.getContent().read(responseBytes);
		
		Assert.assertNotNull("Response content", responseBytes);
		
		responseBytes = Base64.decode(responseBytes);
		TimeStampResponse tsresponse = new TimeStampResponse(responseBytes);
		tsresponse.validate(timeStampRequest);
	}

	
	@Test(expected=TSPException.class)
	public void testServletRequestInBase64ModeWorksButValidationFailsIfTestedAgainstOtherRequest() 
			throws IOException, TSPException {
		
		CloseableHttpClient client = builder.build();
		HttpHost host = new HttpHost(hostname, port);
		HttpPost post = new HttpPost("/tsa");
		
		TimeStampRequestGenerator generator = new TimeStampRequestGenerator();
		TimeStampRequest timeStampRequest = generator.generate(TSPAlgorithms.SHA1, DigestHelper.digest("this is a test message"));
		TimeStampRequest otherTimeStampRequest = generator.generate(TSPAlgorithms.SHA1, DigestHelper.digest("this is another test message"));
		
		ByteArrayEntity entity = new ByteArrayEntity(Base64.encode(timeStampRequest.getEncoded()));
		
		post.setHeader("Content-Type", TimestampServlet.TIMESTAMP_QUERY_CONTENT_TYPE);
		post.setHeader(TimestampServlet.TRANSFER_ENCODING_HEADER, TimestampServlet.TRANSFER_ENCODING_BASE64);
		post.setEntity(entity);
		
		CloseableHttpResponse response = null;
		response = client.execute(host, post);
		Assert.assertNotNull("response", response);
		Assert.assertEquals("Response code is not as expected", 
				HttpServletResponse.SC_OK, response.getStatusLine().getStatusCode());

		HttpEntity responseEntity = response.getEntity();

		Assert.assertEquals("Response content type not as expected", 
				TimestampServlet.TIMESTAMP_REPLY_CONTENT_TYPE, responseEntity.getContentType().getValue());
		Assert.assertEquals("Response content transfer encoding not as expected", 
				TimestampServlet.TRANSFER_ENCODING_BASE64, 
				response.getFirstHeader(TimestampServlet.TRANSFER_ENCODING_HEADER).getValue());
		
		byte[] responseBytes = new byte[(int)responseEntity.getContentLength()];
		responseEntity.getContent().read(responseBytes);
		
		Assert.assertNotNull("Response content", responseBytes);
		
		responseBytes = Base64.decode(responseBytes);
		TimeStampResponse tsresponse = new TimeStampResponse(responseBytes);
		tsresponse.validate(timeStampRequest);
		tsresponse.validate(otherTimeStampRequest); // This will fail
	}
	

	@AfterClass
	public static void stopServer() throws Exception {
		if (server != null)
			server.stop();
	}
	
}
