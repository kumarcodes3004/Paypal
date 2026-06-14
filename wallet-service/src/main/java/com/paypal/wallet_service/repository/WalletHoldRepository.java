package com.paypal.wallet_service.repository;

import com.paypal.wallet_service.entity.WalletHold;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface WalletHoldRepository extends JpaRepository<WalletHold,Long> {

    Optional<WalletHold>  findByWalletHoldReference(String holdReference);

    Optional<WalletHold> findByStatusAndExpiresAtBefore(String status, LocalDateTime now);
}
