package com.telusko.part29springsecex.repo;

import com.telusko.part29springsecex.model.VideoCallMeeting;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VideoCallMeetingRepository extends MongoRepository<VideoCallMeeting, String> {
    
    // Find all meetings for a user (as participant)
    @Query("{ 'participants': ?0 }")
    List<VideoCallMeeting> findMeetingsByParticipant(String email);
    
    // Find scheduled meetings for a user
    @Query("{ $and: [ { 'participants': ?0 }, { 'status': 'SCHEDULED' } ] }")
    List<VideoCallMeeting> findScheduledMeetingsByParticipant(String email);
    
    // Find meeting between two specific users
    @Query("{ $and: [ " +
           "{ 'participants': { $all: [?0, ?1] } }, " +
           "{ 'status': 'SCHEDULED' } " +
           "] }")
    Optional<VideoCallMeeting> findScheduledMeetingBetweenUsers(String email1, String email2);
    
    // Find all meetings between two users (any status)
    @Query("{ 'participants': { $all: [?0, ?1] } }")
    List<VideoCallMeeting> findAllMeetingsBetweenUsers(String email1, String email2);
    
    // Find meetings created by a user
    @Query("{ 'createdBy': ?0 }")
    List<VideoCallMeeting> findMeetingsCreatedBy(String email);
    
    // Check if user has any scheduled meetings
    @Query("{ $and: [ { 'participants': ?0 }, { 'status': 'SCHEDULED' } ] }")
    List<VideoCallMeeting> findActiveScheduledMeetings(String email);
}
