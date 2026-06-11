package com.paypal.transaction_service.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.paypal.transaction_service.entity.Transaction;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
public class KafkaEventProducer {

    private static final String TOPIC = "txn_initiated";

    private final KafkaTemplate<String, Transaction> kafkaTemplate;

    private final ObjectMapper objectMapper;


    public KafkaEventProducer(KafkaTemplate<String, Transaction> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;

        //register module to handle java 8 date time serialization
        this.objectMapper.registerModule(new JavaTimeModule());
    }


    //send raw string message
    public void sendTransactionEvent(String key, Transaction transaction) {

        System.out.println("Sending to kafak ->topic: " + TOPIC + ", Key: " + ", Message: " + transaction);

        //send is an asynchronous call
        CompletableFuture<SendResult<String, Transaction>> future = kafkaTemplate.send(TOPIC, key, transaction);

        future.thenAccept(result -> {
            RecordMetadata metadata = result.getRecordMetadata();
            System.out.println("Kafka Messagae sent successfully! Topic: " + metadata.topic() + ", Partition: " + metadata.partition());
        }).exceptionally(ex -> {
            System.err.println("Failed to send kafka message: " + ex.getMessage());
            return null;
        });
    }


}
