package com.paypal.wallet_service.service;

import com.paypal.wallet_service.dto.*;
import com.paypal.wallet_service.entity.Transaction;
import com.paypal.wallet_service.entity.Wallet;
import com.paypal.wallet_service.entity.WalletHold;
import com.paypal.wallet_service.exception.InsufficientFundsException;
import com.paypal.wallet_service.exception.NotFoundException;
import com.paypal.wallet_service.repository.TransactionRepository;
import com.paypal.wallet_service.repository.WalletHoldRepository;
import com.paypal.wallet_service.repository.WalletRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class WalletService {

    private final WalletHoldRepository walletHoldRepository;
    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;

    public WalletService(WalletHoldRepository walletHoldRepository, WalletRepository walletRepository, TransactionRepository transactionRepository) {
        this.walletHoldRepository = walletHoldRepository;
        this.walletRepository = walletRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional
    public WalletResponse createWallet(CreateWalletRequest request) {
        Wallet wallet1 = new Wallet(request.getUserId(), request.getCurrency());

        Wallet saved = walletRepository.save(wallet1);

        return new WalletResponse(saved.getId(), saved.getUserId(), saved.getCurrency(), saved.getBalance(), saved.getAvailableBalance());

    }

    @Transactional
    public WalletResponse credit(CreditRequest request) {

        log.info("Credit request recieved : userid: {} , Amount {} , Currency {}", request.getUserId(), request.getAmount(), request.getCurrency());
        Wallet wallet = walletRepository.findByUserIdAndCurrency(request.getUserId(), request.getCurrency())
                .orElseThrow(() -> new NotFoundException("Wallet Not found for user:" + request.getUserId()));

        wallet.setBalance(request.getAmount() + wallet.getBalance());
        wallet.setAvailableBalance(request.getAmount() + wallet.getAvailableBalance());
        Wallet saved = walletRepository.save(wallet);
        Long amount = request.getAmount();

        transactionRepository.save(
                new Transaction(wallet.getId(), "CREDIT", amount, "SUCCESS")
        );

        log.info("Credit done : wallet Id : {} , New Balance : {} , Available Balance {}", saved.getId(), saved.getBalance(), saved.getAvailableBalance());

        return new WalletResponse(saved.getId(), saved.getUserId(), saved.getCurrency(), saved.getBalance(), saved.getAvailableBalance());

    }


    public void releaseHold(String ref) {

    }

    @Transactional
    public WalletResponse debit(DebitRequest request) {

        log.info("Debit request recieved: userId {} , Amount : {} , Currency {}", request.getUserId(), request.getAmount(), request.getCurrency());

        //1. Find wallet
        Wallet wallet = walletRepository.findByUserIdAndCurrency(request.getUserId(), request.getCurrency())
                .orElseThrow(() -> new RuntimeException("No wallet found for the user: " + request.getUserId()));

        //2. check if balance is available
        if (wallet.getAvailableBalance() < request.getAmount()) {
            throw new InsufficientFundsException("Not enough Balance");
        }

        //3. subtract the amount from current balance
        wallet.setBalance(wallet.getBalance() - request.getAmount());
        wallet.setAvailableBalance(wallet.getAvailableBalance() - request.getAmount());

        //4.Save balance
        Wallet saved = walletRepository.save(wallet);

        log.info("Debit done: walletID: {} , newBalance : {} , availableBalance {}", saved.getId(), saved.getBalance(), saved.getAvailableBalance());

        //5. return WalletResponse
        return new WalletResponse(saved.getId(), saved.getUserId(), saved.getCurrency(), saved.getBalance(), saved.getAvailableBalance());

    }

    public WalletResponse getWallet(Long userId) {
        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException("Wallet not find for the user: " + userId));

        return new WalletResponse(wallet.getId(), wallet.getUserId(), wallet.getCurrency(), wallet.getBalance(), wallet.getAvailableBalance());
    }


    @Transactional
    public HoldResponse placeHold(HoldRequest request) {
        log.info("Hold request recieved: userId {} , Amount : {} , Currency {}", request.getUserId(), request.getAmount(), request.getCurrency());

        //1. Find wallet
        Wallet wallet = walletRepository.findByUserIdAndCurrency(request.getUserId(), request.getCurrency())
                .orElseThrow(() -> new RuntimeException("No wallet found for the user: " + request.getUserId()));

        if(wallet.getAvailableBalance() < request.getAmount()){
            throw  new InsufficientFundsException("Not enough money to hold");
        }

        wallet.setAvailableBalance(wallet.getAvailableBalance()- request.getAmount());
        WalletHold hold = new WalletHold();

        hold.setWallet(wallet);
        hold.setAmount(request.getAmount());
        hold.setHoldReference("HOLD-"+System.currentTimeMillis());
        hold.setStatus("ACTIVE");

        walletRepository.save(wallet);
        walletHoldRepository.save(hold);

        return new HoldResponse(hold.getHoldReference(),hold.getAmount(),hold.getStatus());



    }

}
