package com.telusko.part29springsecex.service;

import com.telusko.part29springsecex.model.VideoCallRequest;
import com.telusko.part29springsecex.model.VideoCallMeeting;
import com.telusko.part29springsecex.repo.VideoCallRequestRepository;
import com.telusko.part29springsecex.repo.VideoCallMeetingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class VideoCallService {

    @Autowired
    private VideoCallRequestRepository requestRepository;

    @Autowired
    private VideoCallMeetingRepository meetingRepository;

    /**
     * Send a video call request with all constraints applied
     */
    public VideoCallRequest sendVideoCallRequest(String senderEmail, String receiverEmail, 
                                               LocalDateTime scheduledDateTime, int duration) {
        
        // Validate that the scheduled time is in the future
        if (scheduledDateTime.isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Cannot schedule a meeting in the past");
        }
        
        // Constraint 1: Check for overlapping meetings between these users
        List<VideoCallMeeting> existingMeetings = meetingRepository
            .findAllMeetingsBetweenUsers(senderEmail, receiverEmail);
        
        for (VideoCallMeeting meeting : existingMeetings) {
            if (meeting.getStatus().equals("SCHEDULED")) {
                LocalDateTime existingStart = meeting.getScheduledDateTimeAsLocalDateTime();
                if (existingStart != null) {
                    LocalDateTime existingEnd = existingStart.plusMinutes(meeting.getDuration());
                    LocalDateTime newEnd = scheduledDateTime.plusMinutes(duration);
                    
                    // Check for time overlap
                    boolean overlaps = (scheduledDateTime.isBefore(existingEnd) && newEnd.isAfter(existingStart));
                    
                    if (overlaps) {
                        throw new RuntimeException("This time slot conflicts with an existing meeting. " +
                                                 "Please choose a different time.");
                    }
                }
            }
        }

        // Constraint 2 & 5: Expire all previous pending requests between these users
        List<VideoCallRequest> pendingRequests = requestRepository
            .findPendingRequestsBetweenUsers(senderEmail, receiverEmail);
        
        for (VideoCallRequest request : pendingRequests) {
            request.setStatus("EXPIRED");
            requestRepository.save(request);
        }

        // Create new request
        VideoCallRequest newRequest = new VideoCallRequest(senderEmail, receiverEmail, 
                                                         scheduledDateTime, duration);
        
        VideoCallRequest savedRequest = requestRepository.save(newRequest);
        
        return savedRequest;
    }

    /**
     * Accept a video call request and create a meeting
     */
    public VideoCallMeeting acceptVideoCallRequest(String requestId, String acceptingUserEmail) {
        Optional<VideoCallRequest> requestOpt = requestRepository.findById(requestId);
        
        if (!requestOpt.isPresent()) {
            throw new RuntimeException("Video call request not found");
        }

        VideoCallRequest request = requestOpt.get();
        
        // Validate that the accepting user is the receiver
        if (!request.getReceiverEmail().equals(acceptingUserEmail)) {
            throw new RuntimeException("You are not authorized to accept this request");
        }

        // Check if request is still pending
        if (!"PENDING".equals(request.getStatus())) {
            throw new RuntimeException("This request is no longer pending");
        }

        // Constraint: Check if either user already has a scheduled meeting with someone else
        List<VideoCallMeeting> senderMeetings = meetingRepository
            .findScheduledMeetingsByParticipant(request.getSenderEmail());
        List<VideoCallMeeting> receiverMeetings = meetingRepository
            .findScheduledMeetingsByParticipant(request.getReceiverEmail());

        // Check if there's already a meeting between these specific users
        Optional<VideoCallMeeting> existingMeeting = meetingRepository
            .findScheduledMeetingBetweenUsers(request.getSenderEmail(), request.getReceiverEmail());
        
        if (existingMeeting.isPresent()) {
            throw new RuntimeException("A meeting is already scheduled between these users");
        }

        // Accept the request
        request.setStatus("ACCEPTED");
        requestRepository.save(request);

        // Expire all other pending requests between these users
        List<VideoCallRequest> otherPendingRequests = requestRepository
            .findPendingRequestsBetweenUsers(request.getSenderEmail(), request.getReceiverEmail());
        
        for (VideoCallRequest otherRequest : otherPendingRequests) {
            if (!otherRequest.getId().equals(requestId)) {
                otherRequest.setStatus("EXPIRED");
                requestRepository.save(otherRequest);
            }
        }

        // Create the meeting
        String title = "Video Call Meeting";
        List<String> participants = Arrays.asList(request.getSenderEmail(), request.getReceiverEmail());
        
        VideoCallMeeting meeting = new VideoCallMeeting(title, request.getScheduledDateTimeAsLocalDateTime(), 
                                                       request.getDuration(), participants, 
                                                       request.getSenderEmail());
        meeting.setId(requestId); // Use the same ID as the request for consistency
        
        return meetingRepository.save(meeting);
    }

    /**
     * Reject a video call request
     */
    public void rejectVideoCallRequest(String requestId, String rejectingUserEmail) {
        Optional<VideoCallRequest> requestOpt = requestRepository.findById(requestId);
        
        if (!requestOpt.isPresent()) {
            throw new RuntimeException("Video call request not found");
        }

        VideoCallRequest request = requestOpt.get();
        
        // Validate that the rejecting user is the receiver
        if (!request.getReceiverEmail().equals(rejectingUserEmail)) {
            throw new RuntimeException("You are not authorized to reject this request");
        }

        // Check if request is still pending
        if (!"PENDING".equals(request.getStatus())) {
            throw new RuntimeException("This request is no longer pending");
        }

        // Delete rejected requests immediately - no need to store them
        requestRepository.delete(request);
        System.out.println("Deleted rejected video call request: " + requestId + " (rejected by: " + rejectingUserEmail + ")");
    }

    /**
     * Delete a scheduled meeting (both users can delete)
     */
    public void deleteMeeting(String meetingId, String userEmail) {
        Optional<VideoCallMeeting> meetingOpt = meetingRepository.findById(meetingId);
        
        if (!meetingOpt.isPresent()) {
            throw new RuntimeException("Meeting not found");
        }

        VideoCallMeeting meeting = meetingOpt.get();
        
        // Check if user is a participant
        if (!meeting.getParticipants().contains(userEmail)) {
            throw new RuntimeException("You are not authorized to delete this meeting");
        }

        // Delete the meeting
        meetingRepository.delete(meeting);
        
        // Also update the corresponding request status if it exists
        Optional<VideoCallRequest> requestOpt = requestRepository.findById(meetingId);
        if (requestOpt.isPresent()) {
            VideoCallRequest request = requestOpt.get();
            request.setStatus("CANCELLED");
            requestRepository.save(request);
        }
    }

    /**
     * Scheduled task to automatically delete expired meetings
     * Runs every 30 seconds to ensure immediate cleanup with no history
     */
    @Scheduled(fixedRate = 30000) // 30 seconds = 30,000 milliseconds
    public void cleanupExpiredMeetings() {
        try {
            LocalDateTime now = LocalDateTime.now();
            List<VideoCallMeeting> allMeetings = meetingRepository.findAll();
            
            for (VideoCallMeeting meeting : allMeetings) {
                try {
                    // Calculate meeting end time
                    LocalDateTime scheduledTime = meeting.getScheduledDateTimeAsLocalDateTime();
                    if (scheduledTime != null) {
                        LocalDateTime meetingEnd = scheduledTime.plusMinutes(meeting.getDuration());
                        
                        // Delete meetings immediately when they expire (no buffer)
                        if (meetingEnd.isBefore(now) || meetingEnd.isEqual(now)) {
                            // Delete the meeting completely
                            meetingRepository.delete(meeting);
                            
                            // Also delete any related video call requests to avoid orphaned data
                            List<VideoCallRequest> relatedRequests = requestRepository
                                .findRequestsBetweenUsers(meeting.getParticipants().get(0), 
                                                        meeting.getParticipants().get(1));
                            
                            for (VideoCallRequest request : relatedRequests) {
                                // Delete completed/accepted requests related to this meeting
                                if ("ACCEPTED".equals(request.getStatus()) || "COMPLETED".equals(request.getStatus())) {
                                    requestRepository.delete(request);
                                }
                            }
                            
                            System.out.println("Completely removed expired meeting and related requests: " + meeting.getId() + " (ended at: " + meetingEnd + ")");
                        }
                    }
                } catch (Exception e) {
                    // Log error but continue with other meetings
                    System.err.println("Error processing meeting " + meeting.getId() + ": " + e.getMessage());
                }
            }
        } catch (Exception e) {
            // Log error but don't crash the scheduled task
            System.err.println("Error in cleanupExpiredMeetings: " + e.getMessage());
        }
    }

    /**
     * Additional cleanup for old video call requests
     * Runs every 5 minutes to clean up requests based on status and timing
     * - REJECTED: Already deleted immediately
     * - PENDING/ACCEPTED/CANCELLED: Delete after scheduled time ends
     */
    @Scheduled(fixedRate = 300000) // 5 minutes = 300,000 milliseconds
    public void cleanupOldRequests() {
        try {
            LocalDateTime now = LocalDateTime.now();
            List<VideoCallRequest> allRequests = requestRepository.findAll();
            
            for (VideoCallRequest request : allRequests) {
                try {
                    LocalDateTime requestTime = request.getScheduledDateTimeAsLocalDateTime();
                    if (requestTime != null) {
                        String status = request.getStatus();
                        
                        // Calculate when the meeting would end (scheduled time + duration)
                        LocalDateTime meetingEndTime = requestTime.plusMinutes(request.getDuration());
                        
                        // Delete requests after their scheduled meeting time has passed
                        if (meetingEndTime.isBefore(now) || meetingEndTime.isEqual(now)) {
                            // Keep PENDING/ACCEPTED/CANCELLED until meeting time ends
                            if ("PENDING".equals(status) || "ACCEPTED".equals(status) || "CANCELLED".equals(status)) {
                                requestRepository.delete(request);
                                System.out.println("Deleted expired video call request: " + request.getId() + " (status: " + status + ", ended at: " + meetingEndTime + ")");
                            }
                        }
                        
                        // Also clean up very old requests (24+ hours) regardless of status as safety net
                        LocalDateTime cutoffTime = now.minusHours(24);
                        if (requestTime.isBefore(cutoffTime)) {
                            requestRepository.delete(request);
                            System.out.println("Deleted very old video call request: " + request.getId() + " (older than 24 hours)");
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Error processing request " + request.getId() + ": " + e.getMessage());
                }
            }
        } catch (Exception e) {
            System.err.println("Error in cleanupOldRequests: " + e.getMessage());
        }
    }

    /**
     * Get all video call requests between two users
     */
    public List<VideoCallRequest> getRequestsBetweenUsers(String email1, String email2) {
        return requestRepository.findRequestsBetweenUsers(email1, email2);
    }

    /**
     * Get pending requests for a user
     */
    public List<VideoCallRequest> getPendingRequestsForUser(String userEmail) {
        return requestRepository.findPendingRequestsForUser(userEmail);
    }

    /**
     * Get all scheduled meetings for a user
     */
    public List<VideoCallMeeting> getScheduledMeetingsForUser(String userEmail) {
        return meetingRepository.findScheduledMeetingsByParticipant(userEmail);
    }

    /**
     * Get scheduled meeting between two users
     */
    public Optional<VideoCallMeeting> getScheduledMeetingBetweenUsers(String email1, String email2) {
        return meetingRepository.findScheduledMeetingBetweenUsers(email1, email2);
    }

    /**
     * Get the latest pending request between two users (for frontend display)
     */
    public Optional<VideoCallRequest> getLatestPendingRequestBetweenUsers(String email1, String email2) {
        List<VideoCallRequest> pendingRequests = requestRepository.findPendingRequestsBetweenUsers(email1, email2);
        
        return pendingRequests.stream()
            .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
            .findFirst();
    }
}
