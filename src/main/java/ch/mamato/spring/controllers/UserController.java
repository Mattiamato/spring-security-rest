package ch.mamato.spring.controllers;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.token.ConsumerTokenServices;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ch.mamato.spring.dtos.UserRegistration;
import ch.mamato.spring.entities.Role;
import ch.mamato.spring.entities.User;
import ch.mamato.spring.services.UserService;

@RestController
public class UserController {

	@Autowired
	private UserService userService;

	@Autowired
	private ConsumerTokenServices tokenServices;

	@GetMapping(value = "/users")
	public List<User> users() {
		return userService.findAll();
	}

	@PostMapping(value = "/register")
	public String register(@RequestBody UserRegistration userRegistration) {
		if (!StringUtils.isEmpty(userRegistration.getPassword())
				&& !userRegistration.getPassword().equals(userRegistration.getPasswordConfirmation())) {
			return "The passwords doesn't match.";
		} else if (!StringUtils.isEmpty(userRegistration.getUsername())
				&& userService.findByUsername(userRegistration.getUsername()) != null) {
			return "Username already exists.";
		}

		Pattern pattern = Pattern.compile("[^a-zA-Z0-9]");
		if (pattern.matcher(userRegistration.getUsername()).find()) {
			return "Invalid username.";
		}
		userService.save(new User(userRegistration.getUsername(), userRegistration.getPassword(),
				Arrays.asList(new Role("ROLE_USER"))));
		return "User created";
	}

	@GetMapping(value = "/users/logout")
	public String logout(@RequestParam(value = "access_token") String accessToken) {
		tokenServices.revokeToken(accessToken);
		return "bye";
	}

	@GetMapping(value = "/users/getUsername")
	public String getUsername() {
		return SecurityContextHolder.getContext().getAuthentication().getName();
	}

}
