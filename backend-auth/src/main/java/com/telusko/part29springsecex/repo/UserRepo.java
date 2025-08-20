package com.telusko.part29springsecex.repo;

import com.telusko.part29springsecex.model.Users;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepo extends JpaRepository<Users, Long> {
    Users findByEmail(String email);
}