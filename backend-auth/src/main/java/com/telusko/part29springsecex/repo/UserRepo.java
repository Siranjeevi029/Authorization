package com.telusko.part29springsecex.repo;

import com.telusko.part29springsecex.model.Users;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepo extends MongoRepository<Users, String> {
    Users findByEmail(String email);
}