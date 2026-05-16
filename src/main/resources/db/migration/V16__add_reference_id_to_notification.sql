-- V13__add_reference_id_to_notification.sql
-- Erweiterung: reference_id für Notification-Entity
-- Damit können Notifications direkt auf Chat-Konversationen oder andere Entities verweisen

ALTER TABLE notification
ADD COLUMN reference_id VARCHAR(255) NULL
COMMENT 'Optional: Referenz zu External ID (z.B. Chat-Konversation ID, Booking ID)';

