package com.paypal.reward_service.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name ="reward")
@Data
public class Reward {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long Id;

    private String userId;

    private Double points;

    private LocalDateTime sentAt;

    @Column(unique = true)
    private Long transactionId;

}
