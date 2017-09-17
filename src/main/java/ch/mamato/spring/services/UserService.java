package ch.mamato.spring.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import ch.mamato.spring.entities.User;
import ch.mamato.spring.repositories.UserRepository;

@Service
public class UserService {

	@Autowired
	private UserRepository userRepo;

	@Bean
	public PasswordEncoder getPasswordEncoder() {
		return new BCryptPasswordEncoder();
	}

	public List<User> findAll() {
		return userRepo.findAll();
	}

	public User findByUsername(String username) {
		return userRepo.findByUsername(username);
	}

	public void save(User user) {
		user.setPassword(getPasswordEncoder().encode(user.getPassword()));
		userRepo.save(user);
	}

}
