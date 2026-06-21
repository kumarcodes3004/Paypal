package com.paypal.wallet_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DebitRequest {

    private Long userId;
    private String currency;
    private Long amount;


}
