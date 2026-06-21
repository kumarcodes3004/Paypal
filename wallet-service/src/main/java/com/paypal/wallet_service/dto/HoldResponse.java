package com.paypal.wallet_service.dto;

import lombok.Data;

@Data
public class HoldResponse {
    private String holdReference;
    private Long amount;
    private String status;
}
