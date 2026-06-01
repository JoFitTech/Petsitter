package com.softwareengineering.petsitter.wallet.domain;

public enum PaymentStatus {
    HELD,
    RELEASE_REQUESTED,
    RELEASED,
    REFUNDED,
    LEGACY_UNFUNDED
}
