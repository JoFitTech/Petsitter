package com.softwareengineering.petsitter.wallet.repository;

import com.softwareengineering.petsitter.wallet.domain.WalletTransaction;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WalletTransactionRepository extends JpaRepository<WalletTransaction, UUID> {
    List<WalletTransaction> findTop50ByWalletAccountUserIdOrderByCreatedAtDesc(UUID userId);
}
