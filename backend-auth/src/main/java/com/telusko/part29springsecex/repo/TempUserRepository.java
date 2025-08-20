package com.telusko.part29springsecex.repo;

import com.telusko.part29springsecex.model.TempUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface TempUserRepository extends JpaRepository<TempUser, Long> {
    TempUser findByEmail(String email);
    void deleteByCreatedAtBefore(LocalDateTime time);

}

