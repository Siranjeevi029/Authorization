package com.telusko.part29springsecex.repo;

import com.telusko.part29springsecex.model.VideoCallRequest;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VideoCallRequestRepository extends MongoRepository<VideoCallRequest, String> {
    
    // Find all requests between two users (both directions)
    @Query("{ $or: [ " +
           "{ $and: [ { 'senderEmail': ?0 }, { 'receiverEmail': ?1 } ] }, " +
           "{ $and: [ { 'senderEmail': ?1 }, { 'receiverEmail': ?0 } ] } " +
           "] }")
    List<VideoCallRequest> findRequestsBetweenUsers(String email1, String email2);
    
    // Find pending requests between two users
    @Query("{ $and: [ " +
           "{ 'status': 'PENDING' }, " +
           "{ $or: [ " +
           "{ $and: [ { 'senderEmail': ?0 }, { 'receiverEmail': ?1 } ] }, " +
           "{ $and: [ { 'senderEmail': ?1 }, { 'receiverEmail': ?0 } ] } " +
           "] } ] }")
    List<VideoCallRequest> findPendingRequestsBetweenUsers(String email1, String email2);
    
    // Find all pending requests for a user (as receiver)
    @Query("{ $and: [ { 'receiverEmail': ?0 }, { 'status': 'PENDING' } ] }")
    List<VideoCallRequest> findPendingRequestsForUser(String receiverEmail);
    
    // Find all requests sent by a user
    @Query("{ 'senderEmail': ?0 }")
    List<VideoCallRequest> findRequestsBySender(String senderEmail);
    
    // Find all requests received by a user
    @Query("{ 'receiverEmail': ?0 }")
    List<VideoCallRequest> findRequestsByReceiver(String receiverEmail);
    
    // Find latest request between two users
    @Query(value = "{ $or: [ " +
           "{ $and: [ { 'senderEmail': ?0 }, { 'receiverEmail': ?1 } ] }, " +
           "{ $and: [ { 'senderEmail': ?1 }, { 'receiverEmail': ?0 } ] } " +
           "] }", 
           sort = "{ 'createdAt': -1 }")
    Optional<VideoCallRequest> findLatestRequestBetweenUsers(String email1, String email2);

    List<VideoCallRequest> findBySenderEmailAndReceiverEmailAndStatus(String friendEmail, String email, String pending);
}
