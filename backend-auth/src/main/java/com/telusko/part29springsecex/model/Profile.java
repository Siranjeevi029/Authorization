package com.telusko.part29springsecex.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.FieldType;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.time.LocalDateTime;
import java.util.List;

@Setter
@Getter
@Document(collection = "profiles")
public class Profile {
    @MongoId(FieldType.OBJECT_ID)
    private String id;

    @Indexed(unique = true)
    private String email;

    private String fullName;
    private String profilePicture;
    private String location;
    private Integer age;
    private List<Skill> skillsOffered;
    private List<Skill> skillsWanted;
    private String bio;
    private Double rating;
    private List<String> knownLanguages;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String status;

    @Setter
    @Getter
    public static class Skill {
        private String name;
        private String level;
        private String description;
        private String availability; // Only for skillsOffered
    }
}