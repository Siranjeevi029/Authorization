package com.telusko.part29springsecex.controller;

import com.telusko.part29springsecex.dto.VideoCallRequestDTO;
import com.telusko.part29springsecex.model.VideoCallRequest;
import com.telusko.part29springsecex.model.VideoCallMeeting;
import com.telusko.part29springsecex.service.JWTService;
import com.telusko.part29springsecex.service.VideoCallService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/video-call")
public class VideoCallController {

    private static final Logger logger = LoggerFactory.getLogger(VideoCallController.class);

    @Autowired
    private JWTService jwtService;

    @Autowired
    private VideoCallService videoCallService;

    /**
     * Send a video call request
     */
    @PostMapping("/request")
    public ResponseEntity<?> sendVideoCallRequest(@RequestHeader("Authorization") String token, 
                                                @RequestBody Map<String, Object> request) {
        try {
            String senderEmail = jwtService.extractEmail(token.replace("Bearer ", ""));
            if (senderEmail == null || senderEmail.isBlank()) {
                return ResponseEntity.badRequest().body("Invalid authentication token");
            }

            // Validate request data
            if (!request.containsKey("receiverEmail") || !request.containsKey("scheduledDateTime") || !request.containsKey("duration")) {
                return ResponseEntity.badRequest().body("Missing required fields: receiverEmail, scheduledDateTime, duration");
            }

            String receiverEmail = (String) request.get("receiverEmail");
            String scheduledDateTimeStr = (String) request.get("scheduledDateTime");
            Integer duration = (Integer) request.get("duration");

            // Parse the datetime
            LocalDateTime scheduledDateTime = LocalDateTime.parse(scheduledDateTimeStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);

            // Validate that the scheduled time is in the future
            if (scheduledDateTime.isBefore(LocalDateTime.now())) {
                return ResponseEntity.badRequest().body("Cannot schedule a meeting in the past");
            }

            // Validate duration (15-60 minutes)
            if (duration < 15 || duration > 60) {
                return ResponseEntity.badRequest().body("Duration must be between 15 and 60 minutes");
            }

            VideoCallRequest videoCallRequest = videoCallService.sendVideoCallRequest(
                senderEmail, receiverEmail, scheduledDateTime, duration);

            logger.info("Video call request sent from {} to {} for {}", senderEmail, receiverEmail, scheduledDateTime);
            return ResponseEntity.ok(videoCallRequest);

        } catch (Exception e) {
            logger.error("Error sending video call request: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Accept a video call request
     */
    @PostMapping("/accept/{requestId}")
    public ResponseEntity<?> acceptVideoCallRequest(@RequestHeader("Authorization") String token, 
                                                  @PathVariable String requestId) {
        try {
            String userEmail = jwtService.extractEmail(token.replace("Bearer ", ""));
            if (userEmail == null || userEmail.isBlank()) {
                return ResponseEntity.badRequest().body("Invalid authentication token");
            }

            VideoCallMeeting meeting = videoCallService.acceptVideoCallRequest(requestId, userEmail);
            
            logger.info("Video call request {} accepted by {}", requestId, userEmail);
            return ResponseEntity.ok(meeting);

        } catch (Exception e) {
            logger.error("Error accepting video call request: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Reject a video call request
     */
    @PostMapping("/reject/{requestId}")
    public ResponseEntity<?> rejectVideoCallRequest(@RequestHeader("Authorization") String token, 
                                                  @PathVariable String requestId) {
        try {
            String userEmail = jwtService.extractEmail(token.replace("Bearer ", ""));
            if (userEmail == null || userEmail.isBlank()) {
                return ResponseEntity.badRequest().body("Invalid authentication token");
            }

            videoCallService.rejectVideoCallRequest(requestId, userEmail);
            
            logger.info("Video call request {} rejected by {}", requestId, userEmail);
            return ResponseEntity.ok("Video call request rejected successfully");

        } catch (Exception e) {
            logger.error("Error rejecting video call request: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Delete a scheduled meeting
     */
    @DeleteMapping("/meeting/{meetingId}")
    public ResponseEntity<?> deleteMeeting(@RequestHeader("Authorization") String token, 
                                         @PathVariable String meetingId) {
        try {
            String userEmail = jwtService.extractEmail(token.replace("Bearer ", ""));
            if (userEmail == null || userEmail.isBlank()) {
                return ResponseEntity.badRequest().body("Invalid authentication token");
            }

            videoCallService.deleteMeeting(meetingId, userEmail);
            
            logger.info("Meeting {} deleted by {}", meetingId, userEmail);
            return ResponseEntity.ok("Meeting deleted successfully");

        } catch (Exception e) {
            logger.error("Error deleting meeting: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Get video call requests between two users
     */
    @GetMapping("/requests/{friendEmail}")
    public ResponseEntity<?> getRequestsBetweenUsers(@RequestHeader("Authorization") String token, 
                                                   @PathVariable String friendEmail) {
        try {
            String userEmail = jwtService.extractEmail(token.replace("Bearer ", ""));
            if (userEmail == null || userEmail.isBlank()) {
                return ResponseEntity.badRequest().body("Invalid authentication token");
            }

            List<VideoCallRequest> requests = videoCallService.getRequestsBetweenUsers(userEmail, friendEmail);
            return ResponseEntity.ok(requests);

        } catch (Exception e) {
            logger.error("Error fetching video call requests: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Get scheduled meetings between two users
     */
    @GetMapping("/meetings/{friendEmail}")
    public ResponseEntity<?> getMeetingsBetweenUsers(@RequestHeader("Authorization") String token, 
                                                   @PathVariable String friendEmail) {
        try {
            String userEmail = jwtService.extractEmail(token.replace("Bearer ", ""));
            if (userEmail == null || userEmail.isBlank()) {
                return ResponseEntity.badRequest().body("Invalid authentication token");
            }

            Optional<VideoCallMeeting> meeting = videoCallService.getScheduledMeetingBetweenUsers(userEmail, friendEmail);
            if (meeting.isPresent()) {
                return ResponseEntity.ok(List.of(meeting.get()));
            } else {
                return ResponseEntity.ok(List.of());
            }

        } catch (Exception e) {
            logger.error("Error fetching meetings between users: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Get all scheduled meetings for the authenticated user
     */
    @GetMapping("/meetings")
    public ResponseEntity<?> getAllMeetingsForUser(@RequestHeader("Authorization") String token) {
        try {
            String userEmail = jwtService.extractEmail(token.replace("Bearer ", ""));
            if (userEmail == null || userEmail.isBlank()) {
                return ResponseEntity.badRequest().body("Invalid authentication token");
            }

            List<VideoCallMeeting> meetings = videoCallService.getScheduledMeetingsForUser(userEmail);
            return ResponseEntity.ok(meetings);

        } catch (Exception e) {
            logger.error("Error fetching user meetings: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Get pending video call requests for the authenticated user
     */
    @GetMapping("/requests/pending")
    public ResponseEntity<?> getPendingRequests(@RequestHeader("Authorization") String token) {
        try {
            String userEmail = jwtService.extractEmail(token.replace("Bearer ", ""));
            if (userEmail == null || userEmail.isBlank()) {
                return ResponseEntity.badRequest().body("Invalid authentication token");
            }

            List<VideoCallRequest> pendingRequests = videoCallService.getPendingRequestsForUser(userEmail);
            return ResponseEntity.ok(pendingRequests);

        } catch (Exception e) {
            logger.error("Error fetching pending requests: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Get the latest pending request between two users (for chat page display)
     */
    @GetMapping("/requests/{friendEmail}/latest")
    public ResponseEntity<?> getLatestRequestBetweenUsers(@RequestHeader("Authorization") String token, 
                                                        @PathVariable String friendEmail) {
        try {
            String userEmail = jwtService.extractEmail(token.replace("Bearer ", ""));
            if (userEmail == null || userEmail.isBlank()) {
                return ResponseEntity.badRequest().body("Invalid authentication token");
            }

            Optional<VideoCallRequest> latestRequest = videoCallService.getLatestPendingRequestBetweenUsers(userEmail, friendEmail);
            
            if (latestRequest.isPresent()) {
                return ResponseEntity.ok(latestRequest.get());
            } else {
                return ResponseEntity.ok(null);
            }

        } catch (Exception e) {
            logger.error("Error fetching latest request: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<?> healthCheck() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "Video Call Service is running");
        response.put("timestamp", LocalDateTime.now().toString());
        return ResponseEntity.ok(response);
    }
}
