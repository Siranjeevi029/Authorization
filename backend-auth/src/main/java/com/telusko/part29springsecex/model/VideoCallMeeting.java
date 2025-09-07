package com.telusko.part29springsecex.model;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;
import org.springframework.data.mongodb.core.mapping.FieldType;

import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "video_call_meetings")
public class VideoCallMeeting {
    @MongoId(FieldType.OBJECT_ID)
    private String id;
    
    private String title;
    private LocalDateTime scheduledDateTime;
    private int duration; // in minutes
    private List<String> participants; // email addresses
    private String status; // SCHEDULED, COMPLETED, CANCELLED
    private String createdBy; // email of the user who created the meeting
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public VideoCallMeeting() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.status = "SCHEDULED";
    }

    public VideoCallMeeting(String title, LocalDateTime scheduledDateTime, int duration, List<String> participants, String createdBy) {
        this();
        this.title = title;
        this.scheduledDateTime = scheduledDateTime;
        this.duration = duration;
        this.participants = participants;
        this.createdBy = createdBy;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public LocalDateTime getScheduledDateTime() {
        return scheduledDateTime;
    }

    public void setScheduledDateTime(LocalDateTime scheduledDateTime) {
        this.scheduledDateTime = scheduledDateTime;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public List<String> getParticipants() {
        return participants;
    }

    public void setParticipants(List<String> participants) {
        this.participants = participants;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
        this.updatedAt = LocalDateTime.now();
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
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
        return "VideoCallMeeting{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", scheduledDateTime=" + scheduledDateTime +
                ", duration=" + duration +
                ", participants=" + participants +
                ", status='" + status + '\'' +
                ", createdBy='" + createdBy + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
