package com.softwareengineering.petsitter.wallet.dto;

import com.softwareengineering.petsitter.wallet.domain.PaymentStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record BookingPaymentDto(
        BigDecimal amount,
        PaymentStatus status,
        LocalDateTime releaseRequestedAt,
        LocalDateTime automaticReleaseAt
) {
}
