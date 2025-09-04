package com.telusko.part29springsecex.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;
import org.springframework.data.mongodb.core.mapping.FieldType;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Setter
@Getter
@Document(collection = "temp_users")
public class TempUser {
    @MongoId(FieldType.OBJECT_ID)
    private String id;

    private String email;
    private String password;
    private String encodedOtp;

    // TTL index that expires documents after 60 seconds (1 minute)
    @Indexed(name = "createdAt_ttl_index", expireAfterSeconds = 60)
    private Date createdAt;
}