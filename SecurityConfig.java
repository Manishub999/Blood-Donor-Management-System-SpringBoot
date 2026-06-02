package com.example.blooddb;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import jakarta.servlet.Filter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtRequestFilter jwtRequestFilter;

    public SecurityConfig(JwtRequestFilter jwtRequestFilter) {
        this.jwtRequestFilter = jwtRequestFilter;
    }

    // Defines how passwords will be hashed and verified (BCrypt is the standard)
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Allows us to use the authentication manager during the login process
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    // Defines the rules for API access
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // Disable CSRF for API access (standard practice)
            .cors(cors -> cors.disable()) // Disable CORS (we handle it manually with @CrossOrigin)
            
            // Set session management to stateless (crucial for JWT)
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            
            // Define access control rules
            .authorizeHttpRequests(auth -> auth
                
                // Allow public access to all static frontend assets (HTML, CSS, JS)
                .requestMatchers("/", "/index.html", "/style.css", "/main.js", "/*.html", "/*.js", "/assets/**", "/donor-registered.html").permitAll()

                // Allow public access to specific registration/request forms
                .requestMatchers("/donors", "/requests").permitAll()

                // Allow public access to the login endpoint to get a token
                .requestMatchers("/auth/login").permitAll()
                
                // Secure all other endpoints (like PUT, DELETE, and GET /donors/search)
                .anyRequest().authenticated()
            )
            
            // Add our custom JWT filter before Spring's default filter
            .addFilterBefore((Filter) jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}