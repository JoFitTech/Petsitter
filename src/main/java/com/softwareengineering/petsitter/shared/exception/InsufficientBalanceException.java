package com.softwareengineering.petsitter.shared.exception;

import java.math.BigDecimal;
import java.util.UUID;

public class InsufficientBalanceException extends BusinessRuleViolationException {

    private final UUID ownerId;
    private final BigDecimal missingAmount;

    public InsufficientBalanceException(UUID ownerId, BigDecimal missingAmount) {
        super("Das Guthaben des Tierhalters reicht für diese Buchung nicht aus.");
        this.ownerId = ownerId;
        this.missingAmount = missingAmount;
    }

    public UUID getOwnerId() {
        return ownerId;
    }

    public BigDecimal getMissingAmount() {
        return missingAmount;
    }
}
