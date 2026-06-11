package com.paypal.notification_service.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.paypal.notification_service.entity.Notification;
import com.paypal.notification_service.entity.Transaction;
import com.paypal.notification_service.repository.NotificationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
public class NotificationConsumer {

    private final NotificationRepository notificationRepository;
    private final ObjectMapper objectMapper;

    public NotificationConsumer(NotificationRepository notificationRepository, ObjectMapper objectMapper) {
        this.notificationRepository = notificationRepository;
        this.objectMapper = objectMapper;
//
//        objectMapper.registerModule(new JavaTimeModule());
//        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }
//
//    @KafkaListener(topics = "txn_initiated",groupId = "notification-group")
//    public void listener(String message) throws JsonProcessingException {
//
//        log.info("Inside Listener");
//        Transaction txn =objectMapper.readValue(message, Transaction.class);
//        log.info("Deserialized Transaction: {} ",txn);
//
//        Notification notification =new Notification();
//        String recieverUserId = txn.getRecieverId();
//        String senderUserId =txn.getSenderId();
//
//        String notify ="Rs."+ txn.getAmount() +" Recieved from  user ID "+senderUserId;
//        notification.setUserId(recieverUserId);
//        notification.setMessage(notify);
//        log.info("Set notification msg {}",notify);
//
//        LocalDateTime now = LocalDateTime.now();
//        notification.setSentAt(now);
//        log.info("set sentAt timestamp: {}", now);
//
//        log.info("saving notifcation :{}", notification);
//        notificationRepository.save(notification);
//
//    }


    //THIS MTD IS AUTOMATIC SPRING DOES EVERYTHING FOR US  CLASS CONVERSION AND ALL THAT
    //THIS MTD THE PRODUCER ADDS A TAG(CLASS NAME FROM WHICH IT WAS PRODUCED HERE FOR EX:-TRANSACTION_SERVICE.ENTITY.TRANSACTION)
    //SPRING ASSUMES CONSUMER WILL USE SAME CLASS TO READ IT THAT WHY WE WERE GETTING ERROR
    //AND HAD TO ADD CONFIGURATION IN YAML FILE (IGNORING THE HEADERS)
    @KafkaListener(topics = "txn_initiated", groupId = "notification-group")
    public void consumeTransaction(Transaction transaction) {
        System.out.println("📥 Received transaction: " + transaction);

        Notification notification = new Notification();
        notification.setUserId(transaction.getSenderId());
        notification.setMessage("💰 ₹" + transaction.getAmount() + " received from user " + transaction.getSenderId());
        notification.setSentAt(LocalDateTime.now());

        notificationRepository.save(notification);
        System.out.println("✅ Notification saved: " + notification);
    }
}
