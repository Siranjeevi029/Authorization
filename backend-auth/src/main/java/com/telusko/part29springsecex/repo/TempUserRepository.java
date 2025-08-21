package com.telusko.part29springsecex.repo;

import com.telusko.part29springsecex.model.TempUser;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TempUserRepository extends MongoRepository<TempUser, String> {
    TempUser findByEmail(String email);
}