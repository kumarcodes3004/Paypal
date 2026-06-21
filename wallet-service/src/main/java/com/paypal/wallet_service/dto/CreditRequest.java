package com.paypal.wallet_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreditRequest {

    private Long userId;
    private Long amount;
    private String currency;

}
