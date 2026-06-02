package com.example.blooddb;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class BlooddbApplication {

	public static void main(String[] args) {
		SpringApplication.run(BlooddbApplication.class, args);
	}

    // NEW: Bean to set up a default admin user on first run
    @Bean
    public CommandLineRunner initUser(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            // Check if the user already exists to prevent duplicates
            if (userRepository.findByUsername("admin").isEmpty()) {
                User user = new User();
                user.setUsername("admin");
                // Hash the password before saving! (password = 'password')
                user.setPassword(passwordEncoder.encode("password"));
                userRepository.save(user);
                System.out.println("--- ADMIN USER CREATED: Username: admin, Password: password ---");
            }
        };
    }
}