package com.telusko.part29springsecex.controller;

import com.telusko.part29springsecex.model.Profile;
import com.telusko.part29springsecex.service.JWTService;
import com.telusko.part29springsecex.service.ProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class ProfileController {

    @Autowired
    private ProfileService profileService;

    @Autowired
    private JWTService jwtService;

    @GetMapping("/user/status")
    public ResponseEntity<String> getUserStatus(@RequestHeader("Authorization") String token) {
        System.out.println(jwtService.extractEmail(token.replace("Bearer ", "")));
        String email = jwtService.extractEmail(token.replace("Bearer ", ""));
        if(email == null || email.isBlank()) {
            System.out.println("JWT email is null or empty");
            return ResponseEntity.badRequest().body("Invalid token: missing email");
        }
        boolean isNewUser = profileService.isNewUser(email);
        return ResponseEntity.ok(isNewUser ? "new" : "existing");
    }

    @PostMapping("/profile")
    public ResponseEntity<Profile> saveProfile(@RequestHeader("Authorization") String token,
                                               @RequestBody Profile profile) {
        String email = jwtService.extractEmail(token.replace("Bearer ", ""));
        profile.setEmail(email);
        Profile savedProfile = profileService.saveProfile(profile);
        return ResponseEntity.ok(savedProfile);
    }
}