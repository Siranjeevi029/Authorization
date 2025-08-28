package com.telusko.part29springsecex.repo;

import com.telusko.part29springsecex.model.FriendRequest;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface FriendRequestRepository extends MongoRepository<FriendRequest, String> {
    List<FriendRequest> findByReceiverEmailAndStatus(String receiverEmail, String status);
    List<FriendRequest> findBySenderEmailAndReceiverEmailAndStatus(String senderEmail, String receiverEmail, String status);
    List<FriendRequest> findBySenderEmailAndStatusOrReceiverEmailAndStatus(String senderEmail, String senderStatus, String receiverEmail, String receiverStatus);
}