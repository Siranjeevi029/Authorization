package com.telusko.part29springsecex.config;

import com.telusko.part29springsecex.repo.TempUserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class OtpCleanupTask {

    @Autowired
    private TempUserRepository tempUserRepo;
//1:30 refresh   1 minute
    @Scheduled(fixedRate = 60 * 1000)
    @Transactional
    public void cleanOldOtps() {
        tempUserRepo.deleteByCreatedAtBefore(LocalDateTime.now().minusMinutes(1));
    }
}

