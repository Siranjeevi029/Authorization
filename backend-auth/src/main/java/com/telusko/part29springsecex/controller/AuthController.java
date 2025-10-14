package com.telusko.part29springsecex.controller;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.telusko.part29springsecex.model.Users;
import com.telusko.part29springsecex.repo.UserRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.Date;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin("*")
public class AuthController {
    private static final Logger log = LoggerFactory.getLogger(AuthController.class);
    private final String googleClientId;
    private final String jwtSecret;
    
    @Autowired
    private UserRepo userRepo;

    public AuthController(
            @Value("${google.client.id}") String googleClientId,
            @Value("${jwt.secret}") String jwtSecret) {
        this.googleClientId = googleClientId;
        this.jwtSecret = jwtSecret;
    }

    @PostMapping("/google")
    public ResponseEntity<Map<String, String>> googleAuth(@RequestBody GoogleLoginRequest request)
            throws GeneralSecurityException, IOException {

        if (request.token() == null || request.token().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Token is required"));
        }

        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                new NetHttpTransport(),
                new GsonFactory())
                .setAudience(Collections.singletonList(googleClientId))
                .build();

        GoogleIdToken idToken = verifier.verify(request.token());
        if (idToken == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid ID token"));
        }

        GoogleIdToken.Payload payload = idToken.getPayload();
        String email = payload.getEmail();

        // Save or update user in userRepo
        Users user = userRepo.findByEmail(email);
        if (user == null) {
            user = new Users(email, null, "google");
            userRepo.save(user);
            log.info("New OAuth user saved: {}", email);
        } else if (!"google".equals(user.getProvider())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email already registered with another provider"));
        }

        log.info("User logged in: {}", email);

        String jwtToken = JWT.create()
                .withSubject(email)
                .withClaim("source", "google")
                .withExpiresAt(new Date(System.currentTimeMillis() + 86400000))
                .sign(Algorithm.HMAC256(jwtSecret));

        return ResponseEntity.ok(Map.of(
                "token", jwtToken,
                "email", email
        ));
    }

    public record GoogleLoginRequest(String token) {}
}