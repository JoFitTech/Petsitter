package com.softwareengineering.petsitter.wallet.domain;

public enum RecurringPaymentStatus {
    AWAITING_FUNDS,
    HELD,
    RELEASE_REQUESTED,
    RELEASED,
    REFUNDED,
    SKIPPED
}
