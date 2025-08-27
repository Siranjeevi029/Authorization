package com.telusko.part29springsecex.service;

import com.telusko.part29springsecex.model.Profile;
import com.telusko.part29springsecex.repo.ProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ProfileService {
    @Autowired
    private ProfileRepository profileRepository;

    public boolean isNewUser(String email) {
        Profile profile = profileRepository.findByEmail(email);
        boolean isNew = profile == null || "incomplete".equalsIgnoreCase(profile.getStatus());
        System.out.println("Profile for " + email + ": " + (profile != null ? "Status=" + profile.getStatus() : "null") + ", isNew=" + isNew);
        return isNew;
    }

    public Profile saveProfile(Profile profile) {
        if (profile.getCreatedAt() == null) {
            profile.setCreatedAt(LocalDateTime.now());
        }
        profile.setUpdatedAt(LocalDateTime.now());
        profile.setStatus(isProfileComplete(profile) ? "complete" : "incomplete");
        System.out.println("Saving profile for " + profile.getEmail() + ": Status=" + profile.getStatus());
        return profileRepository.save(profile);
    }

    private boolean isProfileComplete(Profile profile) {
        return profile.getFullName() != null && !profile.getFullName().isEmpty() &&
                profile.getLocation() != null && !profile.getLocation().isEmpty() &&
                profile.getAge() != null && profile.getAge() > 0;
    }
}