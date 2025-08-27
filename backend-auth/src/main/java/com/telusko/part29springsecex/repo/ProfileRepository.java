package com.telusko.part29springsecex.repo;

import com.telusko.part29springsecex.model.Profile;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ProfileRepository extends MongoRepository<Profile, String> {
    Profile findByEmail(String email);
}