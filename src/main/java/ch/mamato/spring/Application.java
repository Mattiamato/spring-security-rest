package ch.mamato.spring;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
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
	public TokenStore tokenStore() {
		return new InMemoryTokenStore();
	}

	@Autowired
	public void authenticationManager(AuthenticationManagerBuilder builder, UserRepository userRepo) throws Exception {
		if (userRepo.count() == 0) {
			userRepo.save(new User("admin", "password", Arrays.asList(new Role("ADMIN"), new Role("USER"))));
		}
		builder.userDetailsService(username -> new CustomUserDetails(userRepo.findByUsername(username)));
	}
}
