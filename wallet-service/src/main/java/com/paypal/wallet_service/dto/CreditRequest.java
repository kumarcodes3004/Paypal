package com.paypal.wallet_service.dto;

import lombok.Data;

@Data
public class CreditRequest {

    private Long userId;
    private Long amount;
    private String currency;

}
