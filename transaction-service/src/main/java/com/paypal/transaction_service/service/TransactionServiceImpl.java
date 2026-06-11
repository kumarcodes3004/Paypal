package com.paypal.transaction_service.service;


import com.paypal.transaction_service.entity.Transaction;
import com.paypal.transaction_service.kafka.KafkaEventProducer;
import com.paypal.transaction_service.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TransactionServiceImpl implements TransactionService{


    private final TransactionRepository transactionRepository;

    private final KafkaEventProducer kafkaEventProducer;

    private final ObjectMapper objectMapper;

    public TransactionServiceImpl(TransactionRepository transactionRepository,ObjectMapper objectMapper,KafkaEventProducer kafkaEventProducer){
        this.transactionRepository =transactionRepository;
        this.objectMapper=objectMapper;
        this.kafkaEventProducer=kafkaEventProducer;
    }

    @Override
    public Transaction createTransaction(Transaction request) {


        Transaction transaction = new Transaction();
        transaction.setSenderId(request.getSenderId());
        transaction.setRecieverId(request.getRecieverId());
        transaction.setAmount(request.getAmount());
        transaction.setTimestamp(LocalDateTime.now());
        transaction.setStatus("SUCCESS");

        Transaction saved =  transactionRepository.save(transaction);
        System.out.println("Saved transaction from DB: "+saved);

        try{
//            String eventPayload =objectMapper.writeValueAsString(saved);
            String key = String.valueOf(saved.getId());
            kafkaEventProducer.sendTransactionEvent(key,saved);
            System.out.println("Kafka message sent");
        }catch (Exception e){
            System.out.println("Failed to send kafka event:"+ e.getMessage());
            e.printStackTrace();
        }

        return  saved;
    }

    @Override
    public List<Transaction> getAllTransactions() {

        return transactionRepository.findAll();
    }


}
