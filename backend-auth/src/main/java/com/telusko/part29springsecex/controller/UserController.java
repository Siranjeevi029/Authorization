package com.telusko.part29springsecex.controller;

import com.telusko.part29springsecex.dto.BodyDTO;
import com.telusko.part29springsecex.model.Users;
import com.telusko.part29springsecex.repo.UserRepo;
import com.telusko.part29springsecex.service.EmailService;
import com.telusko.part29springsecex.service.OtpService;
import com.telusko.part29springsecex.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {

    @Autowired
    private UserService service;

    @Autowired
    private UserRepo repo;

    @Autowired
    private OtpService otpService;

    @Autowired
    private EmailService emailService;

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody Users user) throws Exception {
        String email = user.getEmail();
        
        if (repo.findByEmail(user.getEmail())!=null) {
            throw new RuntimeException("Username already exists");
        }
        
        String otp = otpService.generateOtp();
        String result = otpService.saveOtp(user, otp);
        
        if (result.startsWith("WAIT:")) {
            // Extract wait time from result
            String waitTime = result.substring(5); // Remove "WAIT:" prefix
            return ResponseEntity.badRequest().body("Please wait " + waitTime + " seconds before requesting a new OTP");
        }
        
        System.out.println(user.getEmail()+" "+user.getPassword());
        System.out.println("OTP generated: " + otp + " for email: " + email);
        
        // Send response immediately, then try to send email
        ResponseEntity<String> response = ResponseEntity.ok("otp sent");
        
        // Try to send email in background (non-blocking)
        new Thread(() -> {
            try {
                emailService.sendEmail(email, "Your OTP Code", "Your OTP is: " + otp);
                System.out.println("OTP email sent successfully to: " + email);
            } catch (Exception e) {
                System.err.println("Failed to send OTP email to " + email + ": " + e.getMessage());
                e.printStackTrace();
            }
        }).start();
        
        return response;
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody BodyDTO body) {
        Users user = new Users(body.email, body.password, "local");
        System.out.println(body.email);
        System.out.println(body.password);
        System.out.println(body.message);
        String token = service.verify(user);
        if (token == null) {
            return ResponseEntity.badRequest().body("Invalid credentials");
        }
        return ResponseEntity.ok(token);
    }


}
