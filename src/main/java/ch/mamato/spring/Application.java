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
 * To login: 1) POST:
 * http://localhost:8080/oauth/token?grant_type=password&username=user&password=user
 * will return the token.
 * 
 * 2) GET:
 * http://localhost:8080/private?access_token=*************TOKEN*************
 * will return the private page.
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
					Arrays.asList(new Role("ACTUATOR"), new Role("USER"))));
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
