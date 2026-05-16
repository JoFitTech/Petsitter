package com.softwareengineering.petsitter.chat.service;

/**
 * Functional Interface für Listener-Deregistrierung.
 *
 * Wird von {@link ChatEventBus#register} zurückgegeben, um den Listener
 * wieder zu entfernen (z.B. beim Verlassen der ChatView).
 */
@FunctionalInterface
public interface Registration {
    /**
     * Entfernt den registrierten Listener.
     */
    void remove();
}

