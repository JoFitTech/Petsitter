package com.softwareengineering.petsitter.wallet.dto;

import java.math.BigDecimal;
import java.util.List;

public record WalletSummaryDto(
        BigDecimal availableBalance,
        BigDecimal heldOutgoing,
        BigDecimal expectedIncoming,
        List<WalletTransactionDto> transactions
) {
}
