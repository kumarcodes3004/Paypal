package com.paypal.wallet_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "wallet")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Wallet {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false,unique = true)
    private Long userId;

    @Column(nullable = false, length = 3)
    private String currency="INR";

    @Column(nullable = false)
    private Long balance;

    @Column(nullable = false)
    private Long availableBalance;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;


    public Wallet(Long userId, String currency) {
        this.userId = userId;
        this.currency = currency;
        this.balance = 0L;
        this.availableBalance = 0L;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
}
