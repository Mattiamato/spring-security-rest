package ch.mamato.spring;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.Base64Utils;
import org.springframework.web.context.WebApplicationContext;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
public class OAuthTest {

	private static final String CLIENT_ID_WEBAPP = "webapp";
	private static final String CLIENT_ID_TRUSTED = "my-trusted-client";

	private static final String USERNAME = "admin";
	private static final String PASSWORD = "password";

	private static final String CLIENT_SECRET = "secret";

	@LocalServerPort
	private int port;

	@Autowired
	private WebApplicationContext context;

	@Autowired
	private FilterChainProxy springSecurityFilterChain;

	private MockMvc mvc;

	static {
		// for localhost testing only
		javax.net.ssl.HttpsURLConnection.setDefaultHostnameVerifier(new javax.net.ssl.HostnameVerifier() {

			@Override
			public boolean verify(String hostname, javax.net.ssl.SSLSession sslSession) {
				if (hostname.equals("localhost")) {
					return true;
				}
				return false;
			}
		});
	}

	@Before
	public void setUp() throws NoSuchAlgorithmException, KeyManagementException {
		X509TrustManager x509TrustManager = new X509TrustManager() {
			@Override
			public java.security.cert.X509Certificate[] getAcceptedIssuers() {
				return null;
			}

			@Override
			public void checkClientTrusted(java.security.cert.X509Certificate[] arg0, String arg1)
					throws CertificateException {
			}

			@Override
			public void checkServerTrusted(java.security.cert.X509Certificate[] arg0, String arg1)
					throws CertificateException {
			}
		};

		TrustManager[] trustAllCerts = new TrustManager[] { x509TrustManager };

		SSLContext sslContext = SSLContext.getInstance("SSL");
		sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
		HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());

		mvc = MockMvcBuilders.webAppContextSetup(context).addFilter(springSecurityFilterChain).build();
	}

	@Test
	public void getAccessTokenWebAppClientTest() {
		String authorization = "Basic " + new String(Base64Utils.encode("admin:password".getBytes()));

		try {
			MockHttpServletRequestBuilder request = get("/oauth/authorize").header("Authorization", authorization)
					.param("client_id", CLIENT_ID_WEBAPP).param("response_type", "token")
					.param("redirect_uri", "https://localhost:8443/");

			MockHttpServletResponse response = mvc.perform(request).andReturn().getResponse();

			List<String> headers = response.getHeaders("Location");
			assertTrue(headers.contains("access_token"));
			assertTrue(headers.contains("token_type=bearer"));
			assertTrue(headers.contains("expires_in"));

		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	@Test
	public void getAccessTokenTrustedClientTest() throws Exception {
		String auth = CLIENT_ID_TRUSTED + ":" + CLIENT_SECRET;
		byte[] encodedAuth = Base64Utils.encode(auth.getBytes());
		String authorization = "Basic " + new String(encodedAuth);
		String contentType = MediaType.APPLICATION_JSON + ";charset=UTF-8";

		String content = mvc
				.perform(post("/oauth/token").header("Authorization", authorization)
						.contentType(MediaType.APPLICATION_FORM_URLENCODED).param("username", USERNAME)
						.param("password", PASSWORD).param("grant_type", "password"))
				.andExpect(status().isOk()).andExpect(MockMvcResultMatchers.content().contentType(contentType))
				.andExpect(jsonPath("$.access_token", is(notNullValue())))
				.andExpect(jsonPath("$.token_type", is(equalTo("bearer"))))
				.andExpect(jsonPath("$.refresh_token", is(notNullValue())))
				.andExpect(jsonPath("$.expires_in", is(greaterThan(4000))))
				.andExpect(jsonPath("$.scope", is(equalTo("read write trust")))).andReturn().getResponse()
				.getContentAsString();

		assertTrue(content.contains("access_token"));
	}
}
