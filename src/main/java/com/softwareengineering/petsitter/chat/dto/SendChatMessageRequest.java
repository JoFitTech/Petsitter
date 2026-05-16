package com.softwareengineering.petsitter.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO für das Versenden einer Chat-Nachricht.
 *
 * Wird von der UI gesendet und validiert.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SendChatMessageRequest {

    /**
     * ID der Konversation.
     */
    private String conversationId;

    /**
     * Der Nachrichtentext (max. 1000 Zeichen).
     */
    private String message;

}

