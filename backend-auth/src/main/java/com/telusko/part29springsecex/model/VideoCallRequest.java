package com.telusko.part29springsecex.model;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;
import org.springframework.data.mongodb.core.mapping.FieldType;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

@Document(collection = "video_call_requests")
public class VideoCallRequest {
    @MongoId(FieldType.OBJECT_ID)
    private String id;
    
    private String senderEmail;
    private String receiverEmail;
    private String scheduledDateTime; // Store as ISO string to avoid MongoDB serialization issues
    private int duration; // in minutes
    private String status; // PENDING, ACCEPTED, REJECTED, EXPIRED
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    public VideoCallRequest() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.status = "PENDING";
    }

    public VideoCallRequest(String senderEmail, String receiverEmail, LocalDateTime scheduledDateTime, int duration) {
        this();
        this.senderEmail = senderEmail;
        this.receiverEmail = receiverEmail;
        this.scheduledDateTime = scheduledDateTime.toString(); // Convert to string
        this.duration = duration;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSenderEmail() {
        return senderEmail;
    }

    public void setSenderEmail(String senderEmail) {
        this.senderEmail = senderEmail;
    }

    public String getReceiverEmail() {
        return receiverEmail;
    }

    public void setReceiverEmail(String receiverEmail) {
        this.receiverEmail = receiverEmail;
    }

    public String getScheduledDateTime() {
        return scheduledDateTime;
    }

    public void setScheduledDateTime(String scheduledDateTime) {
        this.scheduledDateTime = scheduledDateTime;
    }

    // Helper method to get LocalDateTime from string
    public LocalDateTime getScheduledDateTimeAsLocalDateTime() {
        if (scheduledDateTime == null) return null;
        
        try {
            // Try ISO format first (2025-09-27T16:30:00)
            return LocalDateTime.parse(scheduledDateTime);
        } catch (java.time.format.DateTimeParseException e1) {
            try {
                // Try parsing as Date string format (Sat Sep 27 16:30:00 IST 2025)
                // Use Locale.ENGLISH to ensure proper parsing of English day/month names
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", java.util.Locale.ENGLISH);
                java.util.Date date = sdf.parse(scheduledDateTime);
                return date.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime();
            } catch (Exception e2) {
                try {
                    // Try without timezone (Sat Sep 27 16:30:00 2025)
                    String dateWithoutTz = scheduledDateTime.replaceAll(" IST ", " ");
                    java.text.SimpleDateFormat sdf2 = new java.text.SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy", java.util.Locale.ENGLISH);
                    java.util.Date date = sdf2.parse(dateWithoutTz);
                    return date.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime();
                } catch (Exception e3) {
                    // Try manual parsing as last resort
                    try {
                        return parseManually(scheduledDateTime);
                    } catch (Exception e4) {
                        System.err.println("Failed to parse scheduledDateTime: " + scheduledDateTime);
                        return null;
                    }
                }
            }
        }
    }
    
    // Manual parsing method for complex date strings
    private LocalDateTime parseManually(String dateStr) {
        // Parse: "Sat Sep 27 16:30:00 IST 2025"
        String[] parts = dateStr.split(" ");
        if (parts.length >= 5) {
            String month = parts[1];
            int day = Integer.parseInt(parts[2]);
            String[] timeParts = parts[3].split(":");
            int hour = Integer.parseInt(timeParts[0]);
            int minute = Integer.parseInt(timeParts[1]);
            int year = Integer.parseInt(parts[parts.length - 1]); // Last part is year
            
            int monthNum = getMonthNumber(month);
            if (monthNum > 0) {
                return LocalDateTime.of(year, monthNum, day, hour, minute);
            }
        }
        throw new RuntimeException("Cannot parse date manually: " + dateStr);
    }
    
    private int getMonthNumber(String month) {
        switch (month.toLowerCase()) {
            case "jan": return 1;
            case "feb": return 2;
            case "mar": return 3;
            case "apr": return 4;
            case "may": return 5;
            case "jun": return 6;
            case "jul": return 7;
            case "aug": return 8;
            case "sep": return 9;
            case "oct": return 10;
            case "nov": return 11;
            case "dec": return 12;
            default: return -1;
        }
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
        this.updatedAt = LocalDateTime.now();
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "VideoCallRequest{" +
                "id='" + id + '\'' +
                ", senderEmail='" + senderEmail + '\'' +
                ", receiverEmail='" + receiverEmail + '\'' +
                ", scheduledDateTime=" + scheduledDateTime +
                ", duration=" + duration +
                ", status='" + status + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
