package com.telusko.part29springsecex.controller;

import com.telusko.part29springsecex.model.TempUser;
import com.telusko.part29springsecex.model.Users;
import com.telusko.part29springsecex.repo.TempUserRepository;
import com.telusko.part29springsecex.service.OtpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.Map;

@RestController
@RequestMapping("/otp")
public class OtpController {

    @Autowired
    private OtpService otpService;
    
    @Autowired
    private TempUserRepository tempUserRepository;

    @PostMapping("/verify")
    public ResponseEntity<String> verifyOtp(@RequestBody Map<String, String> request) {
        System.out.println(request);
        String email = request.get("email");
        String otp = request.get("otp");

        if (otpService.verifyOtp(email, otp)!=null) {
            return ResponseEntity.ok("Email verified!");
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or expired OTP");
        }
    }
    
    @GetMapping("/timer/{email}")
    public ResponseEntity<Map<String, Object>> getOtpTimer(@PathVariable String email) {
        try {
            TempUser tempUser = tempUserRepository.findByEmail(email);
            if (tempUser == null) {
                return ResponseEntity.ok(Map.of(
                    "error", "No OTP found for this email",
                    "isExpired", true,
                    "timeLeftSeconds", 0
                ));
            }
            
            // Get creation time from database
            Date createdAt = tempUser.getCreatedAt();
            if (createdAt == null) {
                return ResponseEntity.ok(Map.of(
                    "error", "Invalid OTP data",
                    "isExpired", true,
                    "timeLeftSeconds", 0
                ));
            }
            
            // Calculate expiration time (creation time + 60 seconds)
            Date expiresAt = new Date(createdAt.getTime() + 60000);
            long currentTime = System.currentTimeMillis();
            long timeLeftMs = Math.max(0, expiresAt.getTime() - currentTime);
            long timeLeftSeconds = timeLeftMs / 1000;
            
            return ResponseEntity.ok(Map.of(
                "createdAt", createdAt.getTime(),
                "expiresAt", expiresAt.getTime(),
                "currentTime", currentTime,
                "timeLeftMs", timeLeftMs,
                "timeLeftSeconds", timeLeftSeconds,
                "isExpired", timeLeftMs <= 0
            ));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of(
                "error", "Failed to fetch timer: " + e.getMessage(),
                "isExpired", true,
                "timeLeftSeconds", 0
            ));
        }
    }
}
