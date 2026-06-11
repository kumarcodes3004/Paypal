package com.paypal.transaction_service.Controller;

import com.paypal.transaction_service.entity.Transaction;
import com.paypal.transaction_service.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transactions/")
public class TransactionController {


    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService){
        this.transactionService=transactionService;
    }

    @PostMapping("/create")
    public ResponseEntity<?> createTransaction(@Valid @RequestBody Transaction transaction){

       Transaction created = transactionService.createTransaction(transaction);
        return ResponseEntity.ok(created);

    }

    @GetMapping("/all")
    public ResponseEntity<List<Transaction>> getAllTransaction(){

        return ResponseEntity.ok(transactionService.getAllTransactions());
    }
}
