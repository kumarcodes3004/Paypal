package com.paypal.transaction_service.service;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.paypal.transaction_service.client.WalletClient;
import com.paypal.transaction_service.entity.Transaction;
import com.paypal.transaction_service.kafka.KafkaEventProducer;
import com.paypal.transaction_service.repository.TransactionRepository;
import com.paypal.wallet_service.dto.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class TransactionServiceImpl implements TransactionService {


    private final TransactionRepository transactionRepository;

    private final KafkaEventProducer kafkaEventProducer;

    private final ObjectMapper objectMapper;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private WalletClient walletClient;


    public TransactionServiceImpl(TransactionRepository transactionRepository, ObjectMapper objectMapper, KafkaEventProducer kafkaEventProducer) {
        this.transactionRepository = transactionRepository;
        this.objectMapper = objectMapper;
        this.kafkaEventProducer = kafkaEventProducer;
    }

    @Override
    public Transaction createTransaction(Transaction request) {

        log.info("Entered Create Transaction");

        Long senderId = request.getSenderId();
        Long receiverId = request.getRecieverId();
        Long amount = request.getAmount();

        //Step 0 : mark transaction as pending;
        request.setStatus("PENDING");
        request.setTimestamp(LocalDateTime.now());
        Transaction savedTransaction = transactionRepository.save(request);
        log.info("Transaction Pending saved: {} ", savedTransaction);

        String holdRef = null;
        boolean captured = false;// whether capture (actual object) completed;

        try {
            ResponseEntity<HoldResponse> holdResponse = walletClient.hold(
                    new HoldRequest(savedTransaction.getSenderId(), "INR", savedTransaction.getAmount()));

            if (!holdResponse.getStatusCode().is2xxSuccessful() || holdResponse.getBody() == null) {
                throw new RuntimeException("Faield to place Hold: status= " + holdResponse.getStatusCode());
            }

            holdRef = holdResponse.getBody().getHoldReference();

            if (holdRef == null) {
                throw new RuntimeException("Hold response missing holdReference: " + holdResponse.getBody());
            }

            log.info("Hold Placed: {}", holdRef);

            //NEW: Check receiver wallet exists before capture
            try {
                ResponseEntity<WalletResponse> receiverWallet = walletClient.getWallet(request.getRecieverId());

                //if receiver wallet don't exist release hold and mark transaction as Failed
                if (!receiverWallet.getStatusCode().is2xxSuccessful()) {
                    tryReleaseHold(holdRef);
                    log.info("Reciever Wallet missing -> hold released {}", holdRef);
                    savedTransaction.setStatus("FAILED");
                    savedTransaction = transactionRepository.save(savedTransaction);
                    log.info("Transaction Failed (reciever wallet missing): {}", savedTransaction);
                    return savedTransaction;
                }

            } catch (HttpClientErrorException hx) {
                // receiver not found or other 4xx
                log.error("Error encountered when trying to find receiver wallet: {}", hx.getMessage());
            }

            //Step 2 :capture hold ->debit sender wallet

            ResponseEntity<WalletResponse> captureResp = walletClient.capture(
                    new CaptureRequest(holdRef)
            );

            if (!captureResp.getStatusCode().is2xxSuccessful()) {
                log.info("Capture failed : status {} , body = {}", captureResp.getStatusCode(), captureResp.getBody());
                tryReleaseHold(holdRef);
                savedTransaction.setStatus("FAILED");
                savedTransaction = transactionRepository.save(savedTransaction);
                log.info("Transaction Failed (reciever wallet missing): {}", savedTransaction);
                return savedTransaction;
            }

            captured = true;
            log.info("Hold captured ->sender debit");
            try {


                ResponseEntity<WalletResponse> creditResp = walletClient.credit(
                        new CreditRequest(senderId, amount, "INR")
                );

                if (!creditResp.getStatusCode().is2xxSuccessful()) {
                    throw new RuntimeException("Failed to credit receiver: status=" + creditResp.getStatusCode());
                }
                //else success
                log.info("Receiver credited successfully");
            } catch (HttpClientErrorException creditEx) {
                log.error("Credit Failed: {}", creditEx.getMessage());
                try {
                    ResponseEntity<WalletResponse> refundResponse = walletClient.credit(
                            new CreditRequest(senderId, amount, "INR")
                    );
                    if (refundResponse.getStatusCode().is2xxSuccessful()) {
                        log.info("🔁 Compensating refund to sender succeeded");
                    } else {
                        log.error("❌ Compensating refund to sender returned non-2xx: {}", refundResponse.getStatusCode());
                    }
                } catch (Exception ex) {
                    log.error("❌ Compensating refund to sender failed: {}", ex.getMessage());
                }

                savedTransaction.setStatus("FAILED");
                savedTransaction = transactionRepository.save(savedTransaction);
                log.info("❌ Transaction FAILED (credit failed & refunded sender): {}", savedTransaction);
                return savedTransaction;

            }

            savedTransaction.setStatus("SUCCESS");
            savedTransaction=transactionRepository.save(savedTransaction);
            log.info("✅ Transaction SUCCESS: {}", savedTransaction));
            return savedTransaction;
        } catch (HttpClientErrorException e) {
            System.err.println("❌ Wallet service returned error: " + e.getResponseBodyAsString());
            if (holdRef != null && !captured) {
                tryReleaseHold(holdRef);
            }
            savedTransaction.setStatus("FAILED");
            savedTransaction = transactionRepository.save(savedTransaction);
            System.out.println("❌ Transaction FAILED saved (4xx): " + savedTransaction);
            return savedTransaction;
        } catch (Exception e) {
            System.err.println("❌ Transaction failed: " + e.getMessage());
            if (holdRef != null && !captured) {
                tryReleaseHold( holdRef);
            }
            savedTransaction.setStatus("FAILED");
            savedTransaction = transactionRepository.save(savedTransaction);
            System.out.println("❌ Transaction FAILED saved: " + savedTransaction);
            return savedTransaction;
        }

        try {
//            String eventPayload =objectMapper.writeValueAsString(saved);
            String key = String.valueOf(savedTransaction.getId());
            kafkaEventProducer.sendTransactionEvent(key, savedTransaction);
            System.out.println("Kafka message sent");
        } catch (Exception e) {
            System.out.println("Failed to send kafka event:" + e.getMessage());
            e.printStackTrace();
        }

        return savedTransaction;
    }

    @Override
    public List<Transaction> getAllTransactions() {

        return transactionRepository.findAll();
    }

    private void tryReleaseHold(String holdReference) {
        if (holdReference == null) return;
        try {
            ResponseEntity<HoldResponse> releaseResp = walletClient.releaseHold(holdReference);
            System.out.println("ℹ️ Release response: status=" + releaseResp.getStatusCode() + " body=" + releaseResp.getBody());
        } catch (Exception ex) {
            // Best-effort: log and move on (we don't want the whole transaction to crash on release failure)
            System.err.println("❌ Failed to release hold [" + holdReference + "]: " + ex.getMessage());
        }
    }
}
