package com.softwareengineering.petsitter.wallet.repository;

import com.softwareengineering.petsitter.wallet.domain.WalletAccount;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface WalletAccountRepository extends JpaRepository<WalletAccount, UUID> {
    Optional<WalletAccount> findByUserId(UUID userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select account from WalletAccount account where account.user.id = :userId")
    Optional<WalletAccount> findByUserIdForUpdate(@Param("userId") UUID userId);
}
