-- V19__ensure_notification_table_exists.sql
-- Sicherheitsmigration fuer bestehende Datenbanken, bei denen notification trotz spaeterer Migrationen fehlt.
-- Frische Datenbanken erhalten die Tabelle bereits ueber V12_1; fuer bestehende Datenbanken stellt diese Migration
-- den finalen Tabellenzustand inklusive reference_id idempotent sicher.

CREATE TABLE IF NOT EXISTS notification (
    id BINARY(16) NOT NULL PRIMARY KEY COMMENT 'UUID generiert von JPA',
    recipient_id BINARY(16) NOT NULL COMMENT 'Benutzer, der die Benachrichtigung empfaengt',
    type VARCHAR(50) NOT NULL COMMENT 'Typ der Benachrichtigung (CHAT_MESSAGE, etc.)',
    message VARCHAR(500) NOT NULL COMMENT 'Nachrichtentext',
    is_read BOOLEAN NOT NULL DEFAULT FALSE COMMENT 'Ob die Benachrichtigung gelesen wurde',
    reference_id VARCHAR(255) NULL COMMENT 'Optional: Referenz zu externen Entities (Chat-ID, Booking ID)',
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT 'Zeitstempel der Erstellung',

    KEY idx_notification_recipient_id (recipient_id),
    KEY idx_notification_is_read (is_read),
    KEY idx_notification_created_at (created_at),

    CONSTRAINT fk_notification_recipient
        FOREIGN KEY (recipient_id) REFERENCES user(id)
        ON UPDATE CASCADE
        ON DELETE CASCADE
);

SET @notification_reference_id_exists = (
    SELECT COUNT(*)
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'notification'
      AND column_name = 'reference_id'
);

SET @notification_reference_id_sql = IF(
    @notification_reference_id_exists = 0,
    "ALTER TABLE notification ADD COLUMN reference_id VARCHAR(255) NULL COMMENT 'Optional: Referenz zu externen Entities (Chat-ID, Booking ID)'",
    'SELECT 1'
);

PREPARE notification_reference_id_stmt FROM @notification_reference_id_sql;
EXECUTE notification_reference_id_stmt;
DEALLOCATE PREPARE notification_reference_id_stmt;

