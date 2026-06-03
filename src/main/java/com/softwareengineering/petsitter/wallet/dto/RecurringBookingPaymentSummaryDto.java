package com.softwareengineering.petsitter.wallet.dto;

import com.softwareengineering.petsitter.wallet.domain.RecurringPaymentStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record RecurringBookingPaymentSummaryDto(
        RecurringPaymentStatus status,
        int payableOccurrences,
        BigDecimal payableAmount,
        LocalDateTime releaseRequestedAt,
        LocalDateTime automaticReleaseAt
) {
    public RecurringBookingPaymentSummaryDto {
        payableAmount = payableAmount == null ? BigDecimal.ZERO : payableAmount;
    }

    public static RecurringBookingPaymentSummaryDto empty() {
        return new RecurringBookingPaymentSummaryDto(null, 0, BigDecimal.ZERO, null, null);
    }
}
