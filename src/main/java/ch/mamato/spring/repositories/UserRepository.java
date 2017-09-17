package ch.mamato.spring.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import ch.mamato.spring.entities.User;

public interface UserRepository extends JpaRepository<User, Long> {

	User findByUsername(String username);

}
