package com.paypal.wallet_service.scheduler;

import com.paypal.wallet_service.entity.WalletHold;
import com.paypal.wallet_service.repository.WalletHoldRepository;
import com.paypal.wallet_service.service.WalletService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
public class HoldExpiryScheduler {

    private final WalletHoldRepository walletHoldRepository;

    private  final WalletService walletService;

    public HoldExpiryScheduler(WalletHoldRepository walletHoldRepository, WalletService walletService) {
        this.walletHoldRepository = walletHoldRepository;
        this.walletService = walletService;
    }


    @Scheduled(fixedRateString = "${wallet.hold.expiry.scan-rate-ms:60000}")
    public void expireOldHolds(){

        LocalDateTime now =LocalDateTime.now();

        //simple :fetch expired active holds (OK for small data sets)
       List<WalletHold> expired = walletHoldRepository.findByStatusAndExpiresAtBefore("ACTIVE",now);

       for(WalletHold hold: expired){
            String ref = hold.getHoldReference();
            try{
                //reuse existing release logic (locks ,audit, idempotency)
                walletService.releaseHold(ref);
                log.info("Expired hold released: ",ref);
            } catch(Exception e){

                log.error("Failed to release expired hold "+ ref + " : "+e.getMessage());
            }
       }

    }

}
