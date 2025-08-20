package com.telusko.part29springsecex.model;



import jdk.jfr.Enabled;
import lombok.Getter;
import lombok.Setter;


@Setter
@Getter
public class EmailRequest {
    private String to;
    private String subject;
    private String body;

    public EmailRequest() {
    }

}


