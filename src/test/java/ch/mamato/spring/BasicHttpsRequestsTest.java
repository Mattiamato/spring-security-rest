package ch.mamato.spring;

import static org.assertj.core.api.Assertions.assertThat;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

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
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import ch.mamato.spring.dtos.UserRegistration;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
public class BasicHttpsRequestsTest {

	@LocalServerPort
	private int port;

	@Autowired
	private TestRestTemplate restTemplate;

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
	}

	@Test
	public void homeShouldReturnDefaultMessage() throws Exception {
		assertThat(this.restTemplate.getForObject("https://localhost:" + port + "/", String.class))
				.contains("Hello, you are not logged in!");
	}

	@Test
	public void usersShouldReturnDefaultMessage() throws Exception {
		assertThat(this.restTemplate.getForObject("https://localhost:" + port + "/users", String.class))
				.isEqualToIgnoringCase("[{\"id\":1,\"username\":\"admin\"}]");
	}

	@Test
	public void registerNewUserTest() throws Exception {
		UserRegistration newUser = new UserRegistration("test", "test_password", "test_password");
		assertThat(this.restTemplate.postForObject("https://localhost:" + port + "/register", newUser, String.class))
				.isEqualToIgnoringCase("User created");
	}

}
