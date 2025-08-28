package com.telusko.part29springsecex.component;

import com.telusko.part29springsecex.model.Profile;
import com.telusko.part29springsecex.repo.ProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class MigrationScript implements CommandLineRunner {
    @Autowired
    private ProfileRepository profileRepository;

    @Override
    public void run(String... args) throws Exception {
        List<Profile> profiles = profileRepository.findAll();
        for (Profile profile : profiles) {
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
            profileRepository.save(profile);
        }
        System.out.println("Migration completed: All skill names converted to lowercase.");
    }
}