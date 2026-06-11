package com.paypal.transaction_service.Dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransferRequest {

    private String senderName;

    private String receiverName;

    private Double amount;
}
