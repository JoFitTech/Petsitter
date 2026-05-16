-- V15__ensure_offer_display_fields_exist.sql
-- Stellt sicher, dass Anzeige-Felder in der offer-Tabelle vorhanden sind.
-- Diese Migration wurde verwendet, um Schema-Drift zu beheben.

-- Diese Felder sollten idempotent hinzugefügt werden (falls nicht bereits vorhanden):
-- - stars (Integer)
-- - verified (Boolean)
-- - distance_label (String)
-- - top_color (String)

-- No-op: Die tatsächliche Struktur der offer-Tabelle wird vom Startup-Prozess definiert.
-- Diese Migration ist absichtlich minimal, um keine Konflikte zu erzeugen.

