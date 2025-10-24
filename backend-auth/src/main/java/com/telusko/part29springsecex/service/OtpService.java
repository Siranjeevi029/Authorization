package com.telusko.part29springsecex.service;

import com.telusko.part29springsecex.model.TempUser;
import com.telusko.part29springsecex.model.Users;
import com.telusko.part29springsecex.repo.TempUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.Random;

@Service
public class OtpService {

    @Autowired
    private UserService userService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private TempUserRepository tempUserRepo;


    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder(12);

    public String generateOtp() {
        int otp = new Random().nextInt(900000) + 100000; // 6-digit
        return String.valueOf(otp);
    }

    public String saveOtp(Users user, String otp) {
        // Check if existing temp user exists and is still valid
        TempUser existingTempUser = tempUserRepo.findByEmail(user.getEmail());
        if(existingTempUser != null) {
            Date createdAt = existingTempUser.getCreatedAt();
            if (createdAt != null) {
                long currentTime = System.currentTimeMillis();
                long expiresAt = createdAt.getTime() + 60000; // 60 seconds
                long timeLeftMs = expiresAt - currentTime;
                
                if (timeLeftMs > 0) {
                    // OTP still valid, return wait message
                    long timeLeftSeconds = timeLeftMs / 1000;
                    return "WAIT:" + timeLeftSeconds;
                }
            }
            // OTP expired, delete it
            tempUserRepo.delete(existingTempUser);
        }

        // Create new OTP
        TempUser tempUser = new TempUser();
        tempUser.setEmail(user.getEmail());
        tempUser.setPassword(user.getPassword()); // Store plain text password, will be encoded during final registration
        tempUser.setEncodedOtp(passwordEncoder.encode(otp));
        tempUser.setCreatedAt(new Date()); // This will trigger TTL deletion after 60 seconds
        tempUserRepo.save(tempUser);
        return "SUCCESS";
    }

    public synchronized Users verifyOtp(String email, String rawOtp) {
        TempUser tempUser = tempUserRepo.findByEmail(email);
        if (tempUser == null) return null;

        boolean matches = passwordEncoder.matches(rawOtp, tempUser.getEncodedOtp());
        
        if (matches) {
            // Check if user already exists to prevent duplicate registration
            Users existingUser = userService.findByEmail(tempUser.getEmail());
            if (existingUser != null) {
                // User already exists, clean up temp user and return existing user
                tempUserRepo.delete(tempUser);
                return existingUser;
            }
            
            // User doesn't exist, create new user
            Users newUser = userService.register(new Users(tempUser.getEmail(), tempUser.getPassword(), "local"));
            tempUserRepo.delete(tempUser);
            return newUser;
        }
        return null;
    }
}
