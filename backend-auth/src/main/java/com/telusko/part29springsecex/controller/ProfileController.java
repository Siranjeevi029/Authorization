package com.telusko.part29springsecex.controller;

import com.telusko.part29springsecex.model.Profile;
import com.telusko.part29springsecex.service.JWTService;
import com.telusko.part29springsecex.service.ProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;

import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class ProfileController {

    @Autowired
    private ProfileService profileService;

    @Autowired
    private JWTService jwtService;

    @Autowired
    private com.telusko.part29springsecex.repo.ProfileRepository profileRepository;

    @GetMapping("/user/status")
    public ResponseEntity<String> getUserStatus(@RequestHeader("Authorization") String token) {
        System.out.println("Status Token: " + token);
        String email = jwtService.extractEmail(token.replace("Bearer ", ""));
        System.out.println("Status Email: " + email);
        if (email == null || email.isBlank()) {
            System.out.println("JWT email is null or empty");
            return ResponseEntity.badRequest().body("Invalid token: missing email");
        }
        boolean isNewUser = profileService.isNewUser(email);
        return ResponseEntity.ok(isNewUser ? "new" : "existing");
    }

    @PostMapping("/profile")
    public ResponseEntity<Profile> saveProfile(@RequestHeader("Authorization") String token,
                                               @RequestBody Profile profile) {
        String email = jwtService.extractEmail(token.replace("Bearer ", ""));
        profile.setEmail(email);

        if (profile.getSkillsOffered() != null) {
            profile.setSkillsOffered(profile.getSkillsOffered().stream()
                    .map(skill -> {
                        Profile.Skill newSkill = new Profile.Skill();
                        newSkill.setName(skill.getName().toLowerCase());
                        return newSkill;
                    })
                    .collect(Collectors.toList()));
        }
        if (profile.getSkillsWanted() != null) {
            profile.setSkillsWanted(profile.getSkillsWanted().stream()
                    .map(skill -> {
                        Profile.Skill newSkill = new Profile.Skill();
                        newSkill.setName(skill.getName().toLowerCase());
                        return newSkill;
                    })
                    .collect(Collectors.toList()));
        }
        Profile savedProfile = profileService.saveProfile(profile);
        return ResponseEntity.ok(savedProfile);
    }

    @GetMapping("/user/profile")
    public ResponseEntity<?> getProfile(@RequestHeader("Authorization") String token) {
        try {
            System.out.println("Profile Token: " + token);
            String email = jwtService.extractEmail(token.replace("Bearer ", ""));
            System.out.println("Profile Email: " + email);
            if (email == null || email.isBlank()) {
                return ResponseEntity.badRequest().body("Invalid token: missing email");
            }
            Profile profile = profileRepository.findByEmail(email);
            System.out.println("Profile Found: " + (profile != null));
            if (profile == null) {
                return ResponseEntity.status(404).body("Profile not found");
            }
            Map<String, Object> response = new HashMap<>();
            response.put("email", profile.getEmail());
            response.put("username", profile.getFullName());
            response.put("skillsOffered", profile.getSkillsOffered().stream()
                    .map(Profile.Skill::getName).collect(Collectors.toList()));
            response.put("skillsWanted", profile.getSkillsWanted().stream()
                    .map(Profile.Skill::getName).collect(Collectors.toList()));
            response.put("bio", profile.getBio());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.out.println("Profile Error: " + e.getMessage());
            return ResponseEntity.status(401).body("Invalid token: " + e.getMessage());
        }
    }

    @GetMapping("/users/matches")
    public ResponseEntity<?> getMatches(@RequestHeader("Authorization") String token) {
        try {
            String email = jwtService.extractEmail(token.replace("Bearer ", ""));
            if (email == null || email.isBlank()) {
                return ResponseEntity.badRequest().body("Invalid token: missing email");
            }
            Profile user = profileRepository.findByEmail(email);
            if (user == null) {
                return ResponseEntity.status(404).body("Profile not found");
            }
            // Extract skill names with null checks
            List<String> skillsOffered = user.getSkillsOffered() != null ?
                    user.getSkillsOffered().stream()
                            .map(Profile.Skill::getName)
                            .filter(name -> name != null)
                            .collect(Collectors.toList()) : List.of();
            List<String> skillsWanted = user.getSkillsWanted() != null ?
                    user.getSkillsWanted().stream()
                            .map(Profile.Skill::getName)
                            .filter(name -> name != null)
                            .collect(Collectors.toList()) : List.of();
            System.out.println("User skillsOffered: " + skillsOffered);
            System.out.println("User skillsWanted: " + skillsWanted);
            // Initialize set for match IDs
            Set<String> matchIds = new HashSet<>();
            // Fetch all profiles once (excluding current user)
            List<Profile> allProfiles = profileRepository.findAll().stream()
                    .filter(profile -> !profile.getEmail().equals(email))
                    .collect(Collectors.toList());
            // Bidirectional partial matching
            for (Profile profile : allProfiles) {
                String profileId = profile.getId();
                boolean isMatch = false;
                // teachYou: profile's skillsOffered matches user's skillsWanted
                if (profile.getSkillsOffered() != null && !skillsWanted.isEmpty()) {
                    boolean matched = false;
                    for (Profile.Skill dbSkill : profile.getSkillsOffered()) {
                        if (dbSkill.getName() == null) continue;
                        for (String userSkill : skillsWanted) {
                            if (userSkill.toLowerCase().contains(dbSkill.getName().toLowerCase()) ||
                                    dbSkill.getName().toLowerCase().contains(userSkill.toLowerCase())) {
                                System.out.println("teachYou match for profile " + profileId + ": dbSkill=" + dbSkill.getName() + ", userSkill=" + userSkill);
                                matched = true;
                                break;
                            }
                        }
                        if (matched) break;
                    }
                    if (matched) isMatch = true;
                }
                // youTeach: profile's skillsWanted matches user's skillsOffered
                if (profile.getSkillsWanted() != null && !skillsOffered.isEmpty()) {
                    boolean matched = false;
                    for (Profile.Skill dbSkill : profile.getSkillsWanted()) {
                        if (dbSkill.getName() == null) continue;
                        for (String userSkill : skillsOffered) {
                            if (userSkill.toLowerCase().contains(dbSkill.getName().toLowerCase()) ||
                                    dbSkill.getName().toLowerCase().contains(userSkill.toLowerCase())) {
                                System.out.println("youTeach match for profile " + profileId + ": dbSkill=" + dbSkill.getName() + ", userSkill=" + userSkill);
                                matched = true;
                                break;
                            }
                        }
                        if (matched) break;
                    }
                    if (matched) isMatch = true;
                }
                if (isMatch) matchIds.add(profileId);
            }
            // Add regex-based matches (user skill as pattern)
            for (String skill : skillsWanted) {
                List<Profile> matches = profileRepository.findBySkillsOfferedNameContaining(skill);
                System.out.println("teachYou regex matches for skill '" + skill + "': " + matches.size() + " profiles");
                for (Profile match : matches) {
                    if (!match.getEmail().equals(email)) {
                        matchIds.add(match.getId());
                    }
                }
            }
            for (String skill : skillsOffered) {
                List<Profile> matches = profileRepository.findBySkillsWantedNameContaining(skill);
                System.out.println("youTeach regex matches for skill '" + skill + "': " + matches.size() + " profiles");
                for (Profile match : matches) {
                    if (!match.getEmail().equals(email)) {
                        matchIds.add(match.getId());
                    }
                }
            }
            // Fetch unique profiles by IDs
            List<Profile> matchProfiles = profileRepository.findAllById(matchIds);
            // Log final size
            System.out.println("matchProfiles size: " + matchProfiles.size());
            // Map to frontend format
            List<Map<String, Object>> matchesResponse = matchProfiles.stream().map(profile -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", profile.getId());
                map.put("username", profile.getFullName());
                map.put("email", profile.getEmail());  // Add this line
                map.put("skillsOffered", profile.getSkillsOffered() != null ?
                        profile.getSkillsOffered().stream().map(Profile.Skill::getName).filter(name -> name != null).collect(Collectors.toList()) : List.of());
                map.put("skillsWanted", profile.getSkillsWanted() != null ?
                        profile.getSkillsWanted().stream().map(Profile.Skill::getName).filter(name -> name != null).collect(Collectors.toList()) : List.of());
                map.put("bio", profile.getBio());
                return map;
            }).collect(Collectors.toList());
            Map<String, List<Map<String, Object>>> response = new HashMap<>();
            response.put("matches", matchesResponse);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.out.println("Matches Error: " + e.getMessage());
            return ResponseEntity.status(401).body("Invalid token: " + e.getMessage());
        }
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@RequestHeader("Authorization") String token,
                                           @RequestBody Profile updatedProfile) {
        try {
            String email = jwtService.extractEmail(token.replace("Bearer ", ""));
            if (email == null || email.isBlank()) {
                return ResponseEntity.badRequest().body("Invalid token: missing email");
            }
            Profile existingProfile = profileRepository.findByEmail(email);
            if (existingProfile == null) {
                return ResponseEntity.status(404).body("Profile not found");
            }
            // Update fields only if provided in the request
            if (updatedProfile.getFullName() != null) {
                existingProfile.setFullName(updatedProfile.getFullName());
            }
            if (updatedProfile.getBio() != null) {
                existingProfile.setBio(updatedProfile.getBio());
            }
            if (updatedProfile.getSkillsOffered() != null) {
                existingProfile.setSkillsOffered(updatedProfile.getSkillsOffered().stream()
                        .map(skill -> {
                            Profile.Skill newSkill = new Profile.Skill();
                            newSkill.setName(skill.getName().toLowerCase());
                            return newSkill;
                        })
                        .collect(Collectors.toList()));
            }
            if (updatedProfile.getSkillsWanted() != null) {
                existingProfile.setSkillsWanted(updatedProfile.getSkillsWanted().stream()
                        .map(skill -> {
                            Profile.Skill newSkill = new Profile.Skill();
                            newSkill.setName(skill.getName().toLowerCase());
                            return newSkill;
                        })
                        .collect(Collectors.toList()));
            }
            Profile savedProfile = profileService.saveProfile(existingProfile);
            return ResponseEntity.ok(savedProfile);
        } catch (Exception e) {
            System.out.println("Update Profile Error: " + e.getMessage());
            return ResponseEntity.status(401).body("Invalid token: " + e.getMessage());
        }
    }

    @GetMapping("/profile/{id}")
    public ResponseEntity<?> getProfileById(@RequestHeader("Authorization") String token, @PathVariable String id) {
        try {
            String email = jwtService.extractEmail(token.replace("Bearer ", ""));
            if (email == null || email.isBlank()) {
                return ResponseEntity.badRequest().body("Invalid token: missing email");
            }
            Optional<Profile> profileOpt = profileRepository.findById(id);
            if (profileOpt.isEmpty()) {
                return ResponseEntity.status(404).body("Profile not found");
            }
            Profile profile = profileOpt.get();
            Map<String, Object> response = new HashMap<>();
            response.put("username", profile.getFullName());
            response.put("email", profile.getEmail());  // Add this line
            response.put("skillsOffered", profile.getSkillsOffered().stream()
                    .map(Profile.Skill::getName).collect(Collectors.toList()));
            response.put("skillsWanted", profile.getSkillsWanted().stream()
                    .map(Profile.Skill::getName).collect(Collectors.toList()));
            response.put("bio", profile.getBio());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.out.println("Get Profile By Id Error: " + e.getMessage());
            return ResponseEntity.status(401).body("Invalid token: " + e.getMessage());
        }
    }


}