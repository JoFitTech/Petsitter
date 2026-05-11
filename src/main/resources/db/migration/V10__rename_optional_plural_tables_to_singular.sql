SET @rename_requests = (
    SELECT IF(
        EXISTS (
            SELECT 1 FROM information_schema.tables
            WHERE table_schema = DATABASE() AND table_name = 'requests'
        ) AND NOT EXISTS (
            SELECT 1 FROM information_schema.tables
            WHERE table_schema = DATABASE() AND table_name = 'request'
        ),
        'RENAME TABLE requests TO `request`',
        'DO 0'
    )
);
PREPARE rename_requests FROM @rename_requests;
EXECUTE rename_requests;
DEALLOCATE PREPARE rename_requests;

SET @rename_bookings = (
    SELECT IF(
        EXISTS (
            SELECT 1 FROM information_schema.tables
            WHERE table_schema = DATABASE() AND table_name = 'bookings'
        ) AND NOT EXISTS (
            SELECT 1 FROM information_schema.tables
            WHERE table_schema = DATABASE() AND table_name = 'booking'
        ),
        'RENAME TABLE bookings TO booking',
        'DO 0'
    )
);
PREPARE rename_bookings FROM @rename_bookings;
EXECUTE rename_bookings;
DEALLOCATE PREPARE rename_bookings;

SET @rename_notifications = (
    SELECT IF(
        EXISTS (
            SELECT 1 FROM information_schema.tables
            WHERE table_schema = DATABASE() AND table_name = 'notifications'
        ) AND NOT EXISTS (
            SELECT 1 FROM information_schema.tables
            WHERE table_schema = DATABASE() AND table_name = 'notification'
        ),
        'RENAME TABLE notifications TO notification',
        'DO 0'
    )
);
PREPARE rename_notifications FROM @rename_notifications;
EXECUTE rename_notifications;
DEALLOCATE PREPARE rename_notifications;
