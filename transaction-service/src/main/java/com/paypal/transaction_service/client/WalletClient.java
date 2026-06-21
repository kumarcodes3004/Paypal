package com.paypal.transaction_service.client;


import com.paypal.wallet_service.dto.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "wallet-service", url = "http://localhost:8086/api/v1/wallets")
public interface WalletClient {

    @PostMapping("/credit")
    ResponseEntity<WalletResponse> credit(@RequestBody CreditRequest request);


    @PostMapping("/debit")
    ResponseEntity<WalletResponse> debit(@RequestBody DebitRequest request);

    @GetMapping("/{userId}")
    ResponseEntity<WalletResponse> getWallet(@PathVariable Long userId);


    @PostMapping("/hold")
    ResponseEntity<HoldResponse> hold(@RequestBody HoldRequest request);


    @PostMapping("/capture")
    ResponseEntity<WalletResponse> capture(@RequestBody CaptureRequest request);


    @PostMapping("/release/{holdReference}")
    ResponseEntity<HoldResponse> releaseHold(@PathVariable String holdReference);


}
