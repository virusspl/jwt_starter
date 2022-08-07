package hq;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import hq.security.JwtUtil;
import hq.service.user.UserService;

@SpringBootApplication
public class HqApplication {

	public static void main(String[] args) {
		SpringApplication.run(HqApplication.class, args);
	}
	
	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
	
	@Bean
	JwtUtil jwtUtil() {
		return new JwtUtil();
	}
	
	@Bean
	CommandLineRunner run(UserService userService) {
		return args -> {
			userService.initDatabase();
		};
	}

}
