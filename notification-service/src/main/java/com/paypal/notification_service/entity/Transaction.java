package com.paypal.notification_service.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name="transaction")
@Data
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sender_user_id", nullable = false)
    private String senderId;

    @Column(name = "reciever_user_id", nullable = false)
    private String recieverId;

    @Column(nullable = false)
    @Positive(message = "Amount must be positive")
    private Double amount;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(nullable = false)
    private String status;

    //Lifecycle callback to set default values before persist
    @PrePersist
    public void prePresist(){
        if(timestamp == null){
            timestamp =LocalDateTime.now();
        }

        if(status == null){
            status="PENDING";
        }
    }
}
