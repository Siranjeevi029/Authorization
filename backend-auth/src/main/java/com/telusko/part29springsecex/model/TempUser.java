package com.telusko.part29springsecex.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;
import org.springframework.data.mongodb.core.mapping.FieldType;

import java.time.LocalDateTime;
import java.util.Date;

@Setter
@Getter
@Document(collection = "temp_users")
public class TempUser {
    @MongoId(FieldType.OBJECT_ID)
    private String id;

    private String email;
    private String password;
    private String encodedOtp;



    // You can use java.util.Date or java.time.Instant
    @Indexed(name = "createdAt_ttl_index")
    private Date createdAt;


}