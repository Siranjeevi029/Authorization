package com.telusko.part29springsecex.controller;

import com.telusko.part29springsecex.service.EmailService;
import com.telusko.part29springsecex.service.OtpService;
import com.telusko.part29springsecex.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/otp")
public class OtpController {



    @Autowired
    private OtpService otpService;

    @Autowired
    private UserService service;

//    @PostMapping("/send")
//    public ResponseEntity<String> sendOtp(@RequestBody Map<String, String> request) throws Exception {
//        String email = request.get("email");
//        String otp = otpService.generateOtp();
//        otpService.saveOtp(email, otp);
//        emailService.sendEmail(email, "Your OTP Code", "Your OTP is: " + otp);
//        return ResponseEntity.ok("OTP sent");
//    }
    //email:"abc@gmail.com"
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
    //email:"abc@gmail.com"
    //otp:"1234455";
}
