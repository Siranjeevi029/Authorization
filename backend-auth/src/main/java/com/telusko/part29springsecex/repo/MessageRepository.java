package com.telusko.part29springsecex.repo;

import com.telusko.part29springsecex.model.Message;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface MessageRepository extends MongoRepository<Message, String> {

    List<Message> findBySenderEmailAndReceiverEmailAndIsReadFalse(String senderEmail, String receiverEmail);

    List<Message> findByReceiverEmailAndIsReadFalse(String receiverEmail);

    List<Message> findBySenderEmailAndReceiverEmailOrSenderEmailAndReceiverEmail(
            String senderEmail1, String receiverEmail1, String senderEmail2, String receiverEmail2);

    @Query(value = "{$or: [{senderEmail: ?0, receiverEmail: ?1}, {senderEmail: ?2, receiverEmail: ?3}]}", sort = "{timestamp: -1}")
    List<Message> findBySenderEmailAndReceiverEmailOrSenderEmailAndReceiverEmailOrderByTimestampDesc(
            String senderEmail1, String receiverEmail1, String senderEmail2, String receiverEmail2);
}