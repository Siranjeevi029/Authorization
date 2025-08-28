package com.telusko.part29springsecex.repo;

import com.telusko.part29springsecex.model.Profile;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface ProfileRepository extends MongoRepository<Profile, String> {
    Profile findByEmail(String email);

    // Find profiles where skillsOffered.name contains the given pattern (case-insensitive)
    @Query("{'skillsOffered.name': { $regex: ?0, $options: 'i' }}")
    List<Profile> findBySkillsOfferedNameContaining(String skillPattern);

    // Find profiles where skillsWanted.name contains the given pattern (case-insensitive)
    @Query("{'skillsWanted.name': { $regex: ?0, $options: 'i' }}")
    List<Profile> findBySkillsWantedNameContaining(String skillPattern);

    @Query("{$or: [{'email': {$in: ?0}}, {'email': {$in: ?1}}]}")
    List<Profile> findFriendsByEmails(List<String> senderEmails, List<String> receiverEmails);
}

