package com.example.blooddb;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*") // Allows your frontend to call this endpoint
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenUtil jwtTokenUtil;
    private final UserDetailsService userDetailsService;

    public AuthController(AuthenticationManager authenticationManager, JwtTokenUtil jwtTokenUtil, UserDetailsService userDetailsService) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenUtil = jwtTokenUtil;
        this.userDetailsService = userDetailsService;
    }

    // Handles the request to /auth/login
    @PostMapping("/login")
    public ResponseEntity<?> createAuthenticationToken(@RequestBody Map<String, String> credentials) throws Exception {
        
        final String username = credentials.get("username");
        final String password = credentials.get("password");

        // 1. Authenticate the user against the hashed password in the database
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
        } catch (Exception e) {
            System.err.println("Authentication Failed: " + e.getMessage());
            return ResponseEntity.status(401).body("Invalid Credentials");
        }

        // 2. If authentication succeeds, load the UserDetails
        final UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        // 3. Generate the JWT token
        final String token = jwtTokenUtil.generateToken(userDetails);

        // 4. Send the token back to the client
        Map<String, String> response = new HashMap<>();
        response.put("token", token);
        return ResponseEntity.ok(response);
    }
}