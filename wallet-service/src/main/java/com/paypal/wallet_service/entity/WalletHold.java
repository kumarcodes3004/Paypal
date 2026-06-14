package com.paypal.wallet_service.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "wallet_holds")
@Data
public class WalletHold {



    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "wallet_id")
    private Wallet wallet;

    @Column(nullable = false)
    private String holdReference; //unique id for each hold

    @Column(nullable = false)
    private Long amount;

    @Column(nullable = false)
    private String status = "ACTIVE"; //ACTIVE ,CAPTURED,RELEASED
    //active -> captured  =>transaction successful, money deducted
    //active -> released => transaction failed/captured -> free money

    @Column(nullable = false)
    private LocalDateTime createdAt =LocalDateTime.now();

    @Column(nullable = false)
    private LocalDateTime expiresAt;
}
