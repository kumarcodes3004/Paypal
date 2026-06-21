package com.paypal.transaction_service.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "transaction")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sender_user_id", nullable = false)
    private Long senderId;

    @Column(name = "reciever_user_id", nullable = false)
    private Long recieverId;

    @Column(nullable = false)
    @Positive(message = "Amount must be positive")
    private Long amount;

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
