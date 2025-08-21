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

    public void saveOtp(Users user, String otp) {
//        if(tempUserRepo.findByEmail(user.getEmail())!=null)tempUserRepo.deleteByEmail(user.getEmail());

        TempUser tempUser = new TempUser();
        tempUser.setEmail(user.getEmail());
        tempUser.setPassword(passwordEncoder.encode(user.getPassword()));
        tempUser.setEncodedOtp(passwordEncoder.encode(otp));
        tempUser.setCreatedAt(new Date());
        tempUserRepo.save(tempUser);
    }

    public Users verifyOtp(String email, String rawOtp) {
        TempUser tempUser = tempUserRepo.findByEmail(email);
        if (tempUser == null) return null;

//        boolean isNotExpired = tempUser.getCreatedAt().isAfter(LocalDateTime.now().minusMinutes(2));
        boolean matches = passwordEncoder.matches(rawOtp, tempUser.getEncodedOtp());
//        isNotExpired &&
        //commanded these two lines coz it won't exist after 1:30
         if( matches){
             Users u =  userService.register(new Users(tempUser.getEmail(), tempUser.getPassword(),"local"));
             tempUserRepo.delete(tempUser);
             return u;
         }
          return null;

    }
}
