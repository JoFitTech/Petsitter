package com.softwareengineering.petsitter.wallet.dto;

import com.softwareengineering.petsitter.wallet.domain.WalletTransactionType;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record WalletTransactionDto(
        WalletTransactionType type,
        BigDecimal amount,
        BigDecimal balanceAfter,
        String description,
        LocalDateTime createdAt
) {
}
