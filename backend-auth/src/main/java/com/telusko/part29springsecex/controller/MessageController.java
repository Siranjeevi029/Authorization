package com.telusko.part29springsecex.controller;

import com.telusko.part29springsecex.model.FriendRequest;
import com.telusko.part29springsecex.model.Message;
import com.telusko.part29springsecex.repo.FriendRequestRepository;
import com.telusko.part29springsecex.repo.MessageRepository;
import com.telusko.part29springsecex.service.JWTService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class MessageController {

    @Autowired
    private JWTService jwtService;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private FriendRequestRepository friendRequestRepository;

    @PostMapping("/message")
    public ResponseEntity<?> sendMessage(@RequestHeader("Authorization") String token, @RequestBody Map<String, String> request) {
        try {
            String senderEmail = jwtService.extractEmail(token.replace("Bearer ", ""));
            String receiverEmail = request.get("receiverEmail");
            String content = request.get("content");
            if (senderEmail == null || senderEmail.isBlank() || receiverEmail == null || content == null) {
                return ResponseEntity.badRequest().body("Missing required fields");
            }
            // Correct friendship check
            List<FriendRequest> direct = friendRequestRepository.findBySenderEmailAndReceiverEmailAndStatus(senderEmail, receiverEmail, "ACCEPTED");
            List<FriendRequest> reverse = friendRequestRepository.findBySenderEmailAndReceiverEmailAndStatus(receiverEmail, senderEmail, "ACCEPTED");
            if (direct.isEmpty() && reverse.isEmpty()) {
                return ResponseEntity.badRequest().body("Users are not friends");
            }
            Message message = new Message();
            message.setSenderEmail(senderEmail);
            message.setReceiverEmail(receiverEmail);
            message.setContent(content);
            message.setTimestamp(LocalDateTime.now());
            messageRepository.save(message);
            return ResponseEntity.ok("Message sent");
        } catch (Exception e) {
            System.out.println("Send Message Error: " + e.getMessage());
            return ResponseEntity.status(401).body("Invalid token: " + e.getMessage());
        }
    }

    @GetMapping("/messages/{friendEmail}")
    public ResponseEntity<?> getMessages(@RequestHeader("Authorization") String token, @PathVariable String friendEmail) {
        try {
            String email = jwtService.extractEmail(token.replace("Bearer ", ""));
            if (email == null || email.isBlank()) {
                return ResponseEntity.badRequest().body("Invalid token: missing email");
            }
            // Correct friendship check
            List<FriendRequest> direct = friendRequestRepository.findBySenderEmailAndReceiverEmailAndStatus(email, friendEmail, "ACCEPTED");
            List<FriendRequest> reverse = friendRequestRepository.findBySenderEmailAndReceiverEmailAndStatus(friendEmail, email, "ACCEPTED");
            if (direct.isEmpty() && reverse.isEmpty()) {
                return ResponseEntity.badRequest().body("Users are not friends");
            }
            List<Message> messages = messageRepository.findBySenderEmailAndReceiverEmailOrSenderEmailAndReceiverEmail(
                    email, friendEmail, friendEmail, email);
            List<Map<String, Object>> response = messages.stream().map(message -> {
                Map<String, Object> map = new HashMap<>();
                map.put("senderEmail", message.getSenderEmail());
                map.put("content", message.getContent());
                map.put("timestamp", message.getTimestamp().toString());
                return map;
            }).collect(Collectors.toList());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.out.println("Get Messages Error: " + e.getMessage());
            return ResponseEntity.status(401).body("Invalid token: " + e.getMessage());
        }
    }
}