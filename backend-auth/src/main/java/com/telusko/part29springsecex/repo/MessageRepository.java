package com.telusko.part29springsecex.repo;

import com.telusko.part29springsecex.model.Message;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface MessageRepository extends MongoRepository<Message, String> {
    List<Message> findBySenderEmailAndReceiverEmailOrSenderEmailAndReceiverEmail(
            String senderEmail1, String receiverEmail1, String senderEmail2, String receiverEmail2);
}