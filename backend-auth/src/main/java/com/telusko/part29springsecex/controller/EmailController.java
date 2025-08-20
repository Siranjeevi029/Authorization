package com.telusko.part29springsecex.controller;






import com.telusko.part29springsecex.model.EmailRequest;
import com.telusko.part29springsecex.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.UnsupportedEncodingException;

@RestController
@CrossOrigin("*")
public class EmailController {

    @Autowired
    EmailService emailService;

    @PostMapping("/email")
    public String sendMail(@RequestBody EmailRequest request) throws Exception {
        emailService.sendEmail(request.getTo(), request.getSubject(), request.getBody());
        return "Email sent";
    }



}
