-- V12_1__create_notification_table.sql
-- Erstellt die Basistabelle fuer Benachrichtigungen vor V13.
-- Die Spalte reference_id wird absichtlich erst in V13 hinzugefuegt.

CREATE TABLE IF NOT EXISTS notification (
    id BINARY(16) NOT NULL PRIMARY KEY COMMENT 'UUID generiert von JPA',
    recipient_id BINARY(16) NOT NULL COMMENT 'Benutzer, der die Benachrichtigung empfängt',
    type VARCHAR(50) NOT NULL COMMENT 'Typ der Benachrichtigung (CHAT_MESSAGE, etc.)',
    message VARCHAR(500) NOT NULL COMMENT 'Nachrichtentext',
    is_read BOOLEAN NOT NULL DEFAULT FALSE COMMENT 'Ob die Benachrichtigung gelesen wurde',
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT 'Zeitstempel der Erstellung',

    KEY idx_notification_recipient_id (recipient_id),
    KEY idx_notification_is_read (is_read),
    KEY idx_notification_created_at (created_at),

    CONSTRAINT fk_notification_recipient
        FOREIGN KEY (recipient_id) REFERENCES user(id)
        ON UPDATE CASCADE
        ON DELETE CASCADE
);


