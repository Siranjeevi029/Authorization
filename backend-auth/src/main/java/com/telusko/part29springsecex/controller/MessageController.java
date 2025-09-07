package com.telusko.part29springsecex.controller;

import com.telusko.part29springsecex.model.FriendRequest;
import com.telusko.part29springsecex.model.Message;
import com.telusko.part29springsecex.model.VideoCallRequest;
import com.telusko.part29springsecex.repo.FriendRequestRepository;
import com.telusko.part29springsecex.repo.MessageRepository;
import com.telusko.part29springsecex.repo.VideoCallRequestRepository;
import com.telusko.part29springsecex.service.JWTService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api")
public class MessageController {

    private static final Logger logger = LoggerFactory.getLogger(MessageController.class);

    @Autowired
    private JWTService jwtService;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private FriendRequestRepository friendRequestRepository;

    @Autowired
    private VideoCallRequestRepository videoCallRequestRepository;

    @PostMapping("/message")
    public ResponseEntity<?> sendMessage(@RequestHeader("Authorization") String token, @RequestBody Map<String, String> request) {
        try {
            String senderEmail = jwtService.extractEmail(token.replace("Bearer ", ""));
            if (senderEmail == null || senderEmail.isBlank() || !request.containsKey("receiverEmail") || !request.containsKey("content")) {
                logger.warn("Missing required fields: senderEmail={}, receiverEmail={}, content={}",
                        senderEmail, request.get("receiverEmail"), request.get("content"));
                return ResponseEntity.badRequest().body("Missing required fields");
            }
            String receiverEmail = request.get("receiverEmail");
            String content = request.get("content");
            List<FriendRequest> direct = friendRequestRepository.findBySenderEmailAndReceiverEmailAndStatus(senderEmail, receiverEmail, "ACCEPTED");
            List<FriendRequest> reverse = friendRequestRepository.findBySenderEmailAndReceiverEmailAndStatus(receiverEmail, senderEmail, "ACCEPTED");
            if (direct.isEmpty() && reverse.isEmpty()) {
                logger.warn("Users are not friends: senderEmail={}, receiverEmail={}", senderEmail, receiverEmail);
                return ResponseEntity.badRequest().body("Users are not friends");
            }
            Message message = new Message();
            message.setSenderEmail(senderEmail);
            message.setReceiverEmail(receiverEmail);
            message.setContent(content);
            message.setTimestamp(LocalDateTime.now());
            message.setRead(false);
            messageRepository.save(message);
            logger.info("Message sent: from={} to={}", senderEmail, receiverEmail);
            return ResponseEntity.ok("Message sent");
        } catch (Exception e) {
            logger.error("Send Message Error: token={}, error={}", token, e.getMessage(), e);
            return ResponseEntity.status(401).body("Invalid token: " + e.getMessage());
        }
    }

    @GetMapping("/messages/{friendEmail}")
    public ResponseEntity<?> getMessages(@RequestHeader("Authorization") String token, @PathVariable String friendEmail) {
        try {
            String email = jwtService.extractEmail(token.replace("Bearer ", ""));
            if (email == null || email.isBlank()) {
                logger.warn("Invalid token: missing email, token={}", token);
                return ResponseEntity.badRequest().body("Invalid token: missing email");
            }
            List<FriendRequest> direct = friendRequestRepository.findBySenderEmailAndReceiverEmailAndStatus(email, friendEmail, "ACCEPTED");
            List<FriendRequest> reverse = friendRequestRepository.findBySenderEmailAndReceiverEmailAndStatus(friendEmail, email, "ACCEPTED");
            if (direct.isEmpty() && reverse.isEmpty()) {
                logger.warn("Users are not friends: email={}, friendEmail={}", email, friendEmail);
                return ResponseEntity.badRequest().body("Users are not friends");
            }
            List<Message> messages = messageRepository.findBySenderEmailAndReceiverEmailOrSenderEmailAndReceiverEmail(
                    email, friendEmail, friendEmail, email);
            List<Map<String, Object>> response = messages.stream().map(message -> {
                Map<String, Object> map = new HashMap<>();
                map.put("senderEmail", message.getSenderEmail());
                map.put("content", message.getContent());
                map.put("timestamp", message.getTimestamp().toString());
                map.put("isRead", message.isRead());
                return map;
            }).collect(Collectors.toList());
            logger.info("Fetched {} messages for email={} and friendEmail={}", response.size(), email, friendEmail);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Get Messages Error: token={}, friendEmail={}, error={}", token, friendEmail, e.getMessage(), e);
            return ResponseEntity.status(401).body("Invalid token: " + e.getMessage());
        }
    }

    @GetMapping("/messages/unread-count")
    public ResponseEntity<?> getUnreadMessagesCount(@RequestHeader("Authorization") String token) {
        try {
            String email = jwtService.extractEmail(token.replace("Bearer ", ""));
            if (email == null || email.isBlank()) {
                logger.warn("Invalid token: missing email, token={}", token);
                return ResponseEntity.badRequest().body("Invalid token: missing email");
            }
            List<Message> messages = messageRepository.findByReceiverEmailAndIsReadFalse(email);
            logger.info("Fetched unread count={} for email={}", messages.size(), email);
            return ResponseEntity.ok(Map.of("count", messages.size()));
        } catch (Exception e) {
            logger.error("Get Unread Messages Count Error: token={}, error={}", token, e.getMessage(), e);
            return ResponseEntity.status(401).body("Invalid token: " + e.getMessage());
        }
    }

    @GetMapping("/messages/unread-counts-per-friend")
    public ResponseEntity<?> getUnreadMessagesCountsPerFriend(@RequestHeader("Authorization") String token) {
        try {
            String email = jwtService.extractEmail(token.replace("Bearer ", ""));
            if (email == null || email.isBlank()) {
                logger.warn("Invalid token: missing email, token={}", token);
                return ResponseEntity.badRequest().body("Invalid token: missing email");
            }
            List<FriendRequest> acceptedRequests = friendRequestRepository.findBySenderEmailAndStatusOrReceiverEmailAndStatus(
                    email, "ACCEPTED", email, "ACCEPTED");
            List<String> friendEmails = acceptedRequests.stream()
                    .map(req -> req.getSenderEmail().equals(email) ? req.getReceiverEmail() : req.getSenderEmail())
                    .collect(Collectors.toList());
            Map<String, Map<String, Object>> response = new HashMap<>();
            for (String friendEmail : friendEmails) {
                List<Message> unreadMessages = messageRepository.findBySenderEmailAndReceiverEmailAndIsReadFalse(friendEmail, email);
                
                // Count pending video call requests from this friend
                int pendingVideoCallRequests = 0;
                try {
                    List<VideoCallRequest> videoCallRequests = videoCallRequestRepository.findBySenderEmailAndReceiverEmailAndStatus(
                            friendEmail, email, "PENDING");
                    pendingVideoCallRequests = videoCallRequests.size();
                } catch (Exception e) {
                    logger.warn("Failed to fetch video call requests for {}: {}", friendEmail, e.getMessage());
                }
                
                List<Message> lastMessages = messageRepository.findBySenderEmailAndReceiverEmailOrSenderEmailAndReceiverEmailOrderByTimestampDesc(
                        email, friendEmail, friendEmail, email);
                // Log all messages to verify sorting
                logger.debug("Messages for {} and {}: {}", email, friendEmail, lastMessages.stream()
                        .map(m -> m.getContent() + " @ " + m.getTimestamp())
                        .collect(Collectors.toList()));
                Optional<Message> lastMessage = lastMessages.isEmpty() ? Optional.empty() : Optional.of(lastMessages.get(0));
                Map<String, Object> friendData = new HashMap<>();
                
                // Include video call requests in unread count
                int totalUnreadCount = unreadMessages.size() + pendingVideoCallRequests;
                friendData.put("unreadCount", totalUnreadCount);
                
                if (lastMessage.isPresent()) {
                    friendData.put("lastMessage", lastMessage.get().getContent());
                    friendData.put("lastMessageTimestamp", lastMessage.get().getTimestamp().toString());
                    friendData.put("lastMessageIsRead", lastMessage.get().isRead());
                } else {
                    friendData.put("lastMessage", null);
                    friendData.put("lastMessageTimestamp", null);
                    friendData.put("lastMessageIsRead", true);
                }
                response.put(friendEmail, friendData);
            }
            logger.info("Fetched unread counts for {} friends for email={}", friendEmails.size(), email);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Get Unread Messages Counts Per Friend Error: token={}, error={}", token, e.getMessage(), e);
            return ResponseEntity.status(401).body("Invalid token: " + e.getMessage());
        }
    }

    @PostMapping("/messages/mark-read/{friendEmail}")
    public ResponseEntity<?> markMessagesAsRead(@RequestHeader("Authorization") String token, @PathVariable String friendEmail) {
        try {
            String email = jwtService.extractEmail(token.replace("Bearer ", ""));
            if (email == null || email.isBlank()) {
                logger.warn("Invalid token: missing email, token={}", token);
                return ResponseEntity.badRequest().body("Invalid token: missing email");
            }
            List<Message> messages = messageRepository.findBySenderEmailAndReceiverEmailAndIsReadFalse(friendEmail, email);
            messages.forEach(msg -> msg.setRead(true));
            messageRepository.saveAll(messages);
            logger.info("Marked {} messages as read for email={} from friendEmail={}", messages.size(), email, friendEmail);
            return ResponseEntity.ok("Messages marked as read");
        } catch (Exception e) {
            logger.error("Mark Messages Read Error: token={}, friendEmail={}, error={}", token, friendEmail, e.getMessage(), e);
            return ResponseEntity.status(401).body("Invalid token: " + e.getMessage());
        }
    }
}