package com.telusko.part29springsecex.service;

import com.telusko.part29springsecex.model.Users;
import com.telusko.part29springsecex.repo.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private JWTService jwtService;

    @Autowired
    AuthenticationManager authManager;

    @Autowired
    private UserRepo repo;

    @Autowired
    private BCryptPasswordEncoder encoder;

    public Users register(Users user) {
        user.setPassword(encoder.encode(user.getPassword()));
        repo.save(user);
        return user;
    }



    public String verify(Users user) {
        try {
            System.out.println("Attempting to verify user: " + user.getEmail());
            System.out.println("Password provided: " + user.getPassword());
            
            // Check if user exists in database
            Users dbUser = repo.findByEmail(user.getEmail());
            if (dbUser == null) {
                System.out.println("User not found in database: " + user.getEmail());
                return null;
            }
            
            System.out.println("User found in database. Stored password: " + dbUser.getPassword());
            
            Authentication authentication = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(user.getEmail(), user.getPassword())
            );
            
            if (authentication.isAuthenticated()) {
                System.out.println("Authentication successful for: " + user.getEmail());
                return jwtService.generateToken(user.getEmail());
            } else {
                System.out.println("Authentication failed for: " + user.getEmail());
                return null;
            }
        } catch (Exception e) {
            System.err.println("Authentication error for " + user.getEmail() + ": " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}
