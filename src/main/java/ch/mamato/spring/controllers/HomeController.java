package ch.mamato.spring.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

	@GetMapping(value = "/")
	public String index() {
		return "Hello, you are not logged in!";
	}

	@GetMapping(value = "/private")
	public String privateArea() {
		return "Hello, you are LOGGED IN!";
	}

}
