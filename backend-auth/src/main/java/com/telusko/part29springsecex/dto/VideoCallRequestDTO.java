package com.telusko.part29springsecex.dto;

import java.time.LocalDateTime;

public class VideoCallRequestDTO {
    private String receiverEmail;
    private LocalDateTime scheduledDateTime;
    private int duration;

    public VideoCallRequestDTO() {}

    public VideoCallRequestDTO(String receiverEmail, LocalDateTime scheduledDateTime, int duration) {
        this.receiverEmail = receiverEmail;
        this.scheduledDateTime = scheduledDateTime;
        this.duration = duration;
    }

    public String getReceiverEmail() {
        return receiverEmail;
    }

    public void setReceiverEmail(String receiverEmail) {
        this.receiverEmail = receiverEmail;
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
}
