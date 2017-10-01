package ch.mamato.spring;

import java.util.Arrays;

import org.apache.catalina.Context;
import org.apache.catalina.connector.Connector;
import org.apache.tomcat.util.descriptor.web.SecurityCollection;
import org.apache.tomcat.util.descriptor.web.SecurityConstraint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.embedded.EmbeddedServletContainerFactory;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.InMemoryTokenStore;

import ch.mamato.spring.entities.Role;
import ch.mamato.spring.entities.User;
import ch.mamato.spring.repositories.UserRepository;

/**
 * GRANT TYPE PASSWORD: 1) POST:
 * http://localhost:8080/oauth/token?grant_type=password&username=user&password=password
 * will return the token.
 * 
 * 2) GET:
 * http://localhost:8080/private?access_token=*************TOKEN*************
 * will return the private page.
 * 
 * 
 * GRANT TYPE IMPLICIT: 1) GET:
 * https://localhost:8443/oauth/authorize?response_type=token&client_id=webapp&redirect_uri=/private
 * 
 * 2) Token is in the redirected link, i.e.:
 * https://localhost:8443/private#access_token=c4edef24-bfe8-4955-be29-e75253c908f8&token_type=bearer&expires_in=42926&scope=read
 * Example:
 * https://auth0.com/docs/api-auth/tutorials/implicit-grant#2-extract-the-access-token
 *
 * 
 * @author Mattia Amato
 *
 */

@SpringBootApplication
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder(11);
	}

	@Bean
	public TokenStore tokenStore() {
		return new InMemoryTokenStore();
	}

	/*
	 * Init user
	 */
	@Autowired
	public void authenticationManager(AuthenticationManagerBuilder builder, UserRepository userRepo) throws Exception {
		if (userRepo.count() == 0) {
			userRepo.save(new User("admin", passwordEncoder().encode("password"),
					Arrays.asList(new Role("ROLE_ACTUATOR"), new Role("ROLE_USER"), new Role("ROLE_CLIENT"))));
		}
		builder.userDetailsService(username -> new CustomUserDetails(userRepo.findByUsername(username)))
				.passwordEncoder(passwordEncoder());
	}

	/*
	 * Redirect HTTP to HTTPS
	 */
	@Bean
	public EmbeddedServletContainerFactory servletContainer() {
		TomcatEmbeddedServletContainerFactory tomcat = new TomcatEmbeddedServletContainerFactory() {
			@Override
			protected void postProcessContext(Context context) {
				SecurityConstraint securityConstraint = new SecurityConstraint();
				securityConstraint.setUserConstraint("CONFIDENTIAL");
				SecurityCollection collection = new SecurityCollection();
				collection.addPattern("/*");
				securityConstraint.addCollection(collection);
				context.addConstraint(securityConstraint);
			}
		};

		tomcat.addAdditionalTomcatConnectors(initiateHttpConnector());
		return tomcat;
	}

	/*
	 * Redirect HTTP to HTTPS
	 */
	private Connector initiateHttpConnector() {
		Connector connector = new Connector(TomcatEmbeddedServletContainerFactory.DEFAULT_PROTOCOL);
		connector.setScheme("http");
		connector.setPort(8080);
		connector.setSecure(false);
		connector.setRedirectPort(8443);

		return connector;
	}
}
