package com.telusko.part29springsecex.service;

import com.telusko.part29springsecex.repo.TempUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class TempUserCleanupService {
    
    private static final Logger logger = LoggerFactory.getLogger(TempUserCleanupService.class);
    
    @Autowired
    private TempUserRepository tempUserRepository;
    
    /**
     * Scheduled task that runs every 30 seconds to clean up expired temp users
     * This is a backup cleanup in case MongoDB TTL doesn't work as expected
     */
    @Scheduled(fixedRate = 30000) // Run every 30 seconds
    public void cleanupExpiredTempUsers() {
        try {
            // Calculate cutoff time (1 minute ago)
            Date cutoffTime = new Date(System.currentTimeMillis() - 60000); // 60 seconds ago
            
            // Find and delete temp users older than 1 minute
            long deletedCount = tempUserRepository.findAll().stream()
                .filter(tempUser -> tempUser.getCreatedAt() != null && tempUser.getCreatedAt().before(cutoffTime))
                .peek(tempUser -> {
                    logger.info("Cleaning up expired temp user: {}", tempUser.getEmail());
                    tempUserRepository.delete(tempUser);
                })
                .count();
                
            if (deletedCount > 0) {
                logger.info("Cleaned up {} expired temp users", deletedCount);
            }
        } catch (Exception e) {
            logger.error("Error during temp user cleanup: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Manual cleanup method that can be called programmatically
     */
    public void performManualCleanup() {
        logger.info("Performing manual temp user cleanup");
        cleanupExpiredTempUsers();
    }
}
