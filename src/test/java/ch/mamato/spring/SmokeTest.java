package ch.mamato.spring;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import ch.mamato.spring.controllers.HomeController;
import ch.mamato.spring.controllers.UserController;
import ch.mamato.spring.entities.User;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SmokeTest {

	Logger logger = LoggerFactory.getLogger(SmokeTest.class);

	@Autowired
	private HomeController homeController;

	@Autowired
	private UserController userController;

	@Test
	public void contextLoads() {
		assertThat(homeController).isNotNull();
		assertThat(userController).isNotNull();
	}

	@Test
	public void shouldFindOneUser() {
		List<User> users = userController.users();
		assertThat(users).size().isEqualTo(1);
		User user = users.get(0);
		assertThat(user.getUsername()).isEqualTo("admin");
	}

}
