package com.telusko.part29springsecex.controller;

import com.telusko.part29springsecex.model.FriendRequest;
import com.telusko.part29springsecex.model.Profile;
import com.telusko.part29springsecex.repo.FriendRequestRepository;
import com.telusko.part29springsecex.repo.ProfileRepository;
import com.telusko.part29springsecex.service.JWTService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/friend")
@CrossOrigin("*")
public class FriendRequestController {

    @Autowired
    private JWTService jwtService;
    @Autowired
    private FriendRequestRepository friendRequestRepository;

    @Autowired
    private ProfileRepository profileRepository;

    @PostMapping("/request")
    public ResponseEntity<?> sendFriendRequest(@RequestHeader("Authorization") String token, @RequestBody Map<String, String> request) {
        try {
            String senderEmail = jwtService.extractEmail(token.replace("Bearer ", ""));
            String receiverEmail = request.get("receiverEmail");
            if (senderEmail == null || senderEmail.isBlank()) {
                return ResponseEntity.badRequest().body("Invalid token: missing email");
            }
            Profile sender = profileRepository.findByEmail(senderEmail);
            Profile receiver = profileRepository.findByEmail(receiverEmail);
            if (sender == null || receiver == null) {
                return ResponseEntity.status(404).body("Sender or receiver profile not found");
            }
            if (senderEmail.equals(receiver.getEmail())) {
                return ResponseEntity.badRequest().body("Cannot send friend request to yourself");
            }
            List<FriendRequest> existingAccepted = friendRequestRepository.findBySenderEmailAndReceiverEmailAndStatus(
                    senderEmail, receiver.getEmail(), "ACCEPTED");
            List<FriendRequest> existingAcceptedReverse = friendRequestRepository.findBySenderEmailAndReceiverEmailAndStatus(
                    receiver.getEmail(), senderEmail, "ACCEPTED");
            if (!existingAccepted.isEmpty() || !existingAcceptedReverse.isEmpty()) {
                return ResponseEntity.badRequest().body("Already friends");
            }
            List<FriendRequest> existingRequests = friendRequestRepository.findBySenderEmailAndReceiverEmailAndStatus(
                    senderEmail, receiver.getEmail(), "PENDING");
            if (!existingRequests.isEmpty()) {
                return ResponseEntity.badRequest().body("Friend request already sent");
            }
            List<FriendRequest> reverseRequests = friendRequestRepository.findBySenderEmailAndReceiverEmailAndStatus(
                    receiver.getEmail(), senderEmail, "PENDING");
            if (!reverseRequests.isEmpty()) {
                FriendRequest reverseRequest = reverseRequests.get(0);
                reverseRequest.setStatus("ACCEPTED");
                reverseRequest.setRead(true); // Mark as read when accepted
                friendRequestRepository.save(reverseRequest);
                return ResponseEntity.ok("You are now friends!");
            }
            FriendRequest friendRequest = new FriendRequest();
            friendRequest.setSenderEmail(senderEmail);
            friendRequest.setReceiverEmail(receiver.getEmail());
            friendRequest.setStatus("PENDING");
            friendRequest.setRead(false); // New requests are unread
            friendRequestRepository.save(friendRequest);
            return ResponseEntity.ok("Friend request sent");
        } catch (Exception e) {
            System.out.println("Send Friend Request Error: " + e.getMessage());
            return ResponseEntity.status(401).body("Invalid token: " + e.getMessage());
        }
    }

    @PostMapping("/request/{requestId}/accept")
    public ResponseEntity<?> acceptFriendRequest(@RequestHeader("Authorization") String token, @PathVariable String requestId) {
        try {
            String email = jwtService.extractEmail(token.replace("Bearer ", ""));
            if (email == null || email.isBlank()) {
                return ResponseEntity.badRequest().body("Invalid token: missing email");
            }
            Optional<FriendRequest> requestOpt = friendRequestRepository.findById(requestId);
            if (requestOpt.isEmpty()) {
                return ResponseEntity.status(404).body("Friend request not found");
            }
            FriendRequest frRequest = requestOpt.get();
            if (!frRequest.getReceiverEmail().equals(email)) {
                return ResponseEntity.badRequest().body("Not authorized to accept this request");
            }
            if (!frRequest.getStatus().equals("PENDING")) {
                return ResponseEntity.badRequest().body("Request is not pending");
            }
            frRequest.setStatus("ACCEPTED");
            frRequest.setRead(true); // Mark as read when accepted
            friendRequestRepository.save(frRequest);
            return ResponseEntity.ok("Friend request accepted");
        } catch (Exception e) {
            System.out.println("Accept Friend Request Error: " + e.getMessage());
            return ResponseEntity.status(401).body("Invalid token: " + e.getMessage());
        }
    }

    @PostMapping("/request/{requestId}/reject")
    public ResponseEntity<?> rejectFriendRequest(@RequestHeader("Authorization") String token, @PathVariable String requestId) {
        try {
            String email = jwtService.extractEmail(token.replace("Bearer ", ""));
            if (email == null || email.isBlank()) {
                return ResponseEntity.badRequest().body("Invalid token: missing email");
            }
            Optional<FriendRequest> requestOpt = friendRequestRepository.findById(requestId);
            if (requestOpt.isEmpty()) {
                return ResponseEntity.status(404).body("Friend request not found");
            }
            FriendRequest frRequest = requestOpt.get();
            if (!frRequest.getReceiverEmail().equals(email)) {
                return ResponseEntity.badRequest().body("Not authorized to reject this request");
            }
            if (!frRequest.getStatus().equals("PENDING")) {
                return ResponseEntity.badRequest().body("Request is not pending");
            }
            frRequest.setRead(true); // Mark as read before deleting
            friendRequestRepository.delete(frRequest);
            return ResponseEntity.ok("Friend request rejected");
        } catch (Exception e) {
            System.out.println("Reject Friend Request Error: " + e.getMessage());
            return ResponseEntity.status(401).body("Invalid token: " + e.getMessage());
        }
    }

    @GetMapping("/requests")
    public ResponseEntity<?> getFriendRequests(@RequestHeader("Authorization") String token) {
        try {
            String email = jwtService.extractEmail(token.replace("Bearer ", ""));
            if (email == null || email.isBlank()) {
                return ResponseEntity.badRequest().body("Invalid token: missing email");
            }
            List<FriendRequest> requests = friendRequestRepository.findByReceiverEmailAndStatus(email, "PENDING");
            List<Map<String, Object>> response = requests.stream().map(req -> {
                Map<String, Object> map = new HashMap<>();
                Profile sender = profileRepository.findByEmail(req.getSenderEmail());
                map.put("requestId", req.getId());
                map.put("senderUsername", sender != null ? sender.getFullName() : "Unknown");
                map.put("senderEmail", req.getSenderEmail());
                map.put("isRead", req.isRead()); // Include read status
                return map;
            }).collect(Collectors.toList());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.out.println("Get Friend Requests Error: " + e.getMessage());
            return ResponseEntity.status(401).body("Invalid token: " + e.getMessage());
        }
    }

    @GetMapping("/requests/unread-count")
    public ResponseEntity<?> getUnreadFriendRequestsCount(@RequestHeader("Authorization") String token) {
        try {
            String email = jwtService.extractEmail(token.replace("Bearer ", ""));
            if (email == null || email.isBlank()) {
                return ResponseEntity.badRequest().body("Invalid token: missing email");
            }
            List<FriendRequest> requests = friendRequestRepository.findByReceiverEmailAndStatusAndIsReadFalse(email, "PENDING");
            return ResponseEntity.ok(Map.of("count", requests.size()));
        } catch (Exception e) {
            System.out.println("Get Unread Friend Requests Count Error: " + e.getMessage());
            return ResponseEntity.status(401).body("Invalid token: " + e.getMessage());
        }
    }

    @PostMapping("/requests/mark-read")
    public ResponseEntity<?> markFriendRequestsAsRead(@RequestHeader("Authorization") String token) {
        try {
            String email = jwtService.extractEmail(token.replace("Bearer ", ""));
            if (email == null || email.isBlank()) {
                return ResponseEntity.badRequest().body("Invalid token: missing email");
            }
            List<FriendRequest> requests = friendRequestRepository.findByReceiverEmailAndStatusAndIsReadFalse(email, "PENDING");
            requests.forEach(req -> req.setRead(true));
            friendRequestRepository.saveAll(requests);
            return ResponseEntity.ok("Friend requests marked as read");
        } catch (Exception e) {
            System.out.println("Mark Friend Requests Read Error: " + e.getMessage());
            return ResponseEntity.status(401).body("Invalid token: " + e.getMessage());
        }
    }

    @GetMapping("/friends")
    public ResponseEntity<?> getFriends(@RequestHeader("Authorization") String token) {
        try {
            String email = jwtService.extractEmail(token.replace("Bearer ", ""));
            if (email == null || email.isBlank()) {
                return ResponseEntity.badRequest().body("Invalid token: missing email");
            }
            List<FriendRequest> acceptedRequests = friendRequestRepository.findBySenderEmailAndStatusOrReceiverEmailAndStatus(
                    email, "ACCEPTED", email, "ACCEPTED");
            List<String> friendEmails = acceptedRequests.stream()
                    .map(req -> req.getSenderEmail().equals(email) ? req.getReceiverEmail() : req.getSenderEmail())
                    .collect(Collectors.toList());
            List<Profile> friends = profileRepository.findFriendsByEmails(friendEmails, friendEmails);
            List<Map<String, Object>> response = friends.stream().map(profile -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", profile.getId());
                map.put("username", profile.getFullName());
                map.put("email", profile.getEmail());
                map.put("skillsOffered", profile.getSkillsOffered() != null ?
                        profile.getSkillsOffered().stream().map(Profile.Skill::getName).filter(name -> name != null).collect(Collectors.toList()) : List.of());
                map.put("skillsWanted", profile.getSkillsWanted() != null ?
                        profile.getSkillsWanted().stream().map(Profile.Skill::getName).filter(name -> name != null).collect(Collectors.toList()) : List.of());
                map.put("bio", profile.getBio());
                return map;
            }).collect(Collectors.toList());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.out.println("Get Friends Error: " + e.getMessage());
            return ResponseEntity.status(401).body("Invalid token: " + e.getMessage());
        }
    }
}