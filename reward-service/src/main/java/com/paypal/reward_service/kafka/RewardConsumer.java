package com.paypal.reward_service.kafka;

import com.paypal.reward_service.entity.Reward;
import com.paypal.reward_service.entity.Transaction;
import com.paypal.reward_service.repository.RewardRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;

@Slf4j
@Component
public class RewardConsumer {


    private final RewardRepository rewardRepository;
    private final ObjectMapper objectMapper;

    public RewardConsumer(RewardRepository rewardRepository, ObjectMapper objectMapper) {
        this.rewardRepository = rewardRepository;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "txn_initiated", groupId = "reward-group")
    public void listener(Transaction transaction) {

        log.info("Reward service listening");

        Reward r1= new Reward();
        r1.setSentAt(LocalDateTime.now());
        r1.setUserId(transaction.getSenderId());
        r1.setTransactionId(transaction.getId());
        r1.setPoints(10.0);

        log.info("Reward created: {}",r1);

        rewardRepository.save(r1);
        log.info("Reward saved {}",r1);

    }

}
