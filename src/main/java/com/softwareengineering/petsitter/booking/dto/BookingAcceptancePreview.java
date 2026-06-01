package com.softwareengineering.petsitter.booking.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record BookingAcceptancePreview(
        UUID ownerId,
        boolean currentUserIsOwner,
        BigDecimal pricePerDay,
        BigDecimal totalPrice,
        BigDecimal availableBalance,
        boolean sufficientBalance
) {
}
