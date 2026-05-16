-- V18__ensure_pet_columns_exist.sql
-- Sicherstellt, dass alle erforderlichen Spalten in der 'pet'-Tabelle vorhanden sind.

ALTER TABLE pet ADD COLUMN birth_date DATE NULL;
ALTER TABLE pet ADD COLUMN custom_species VARCHAR(100) NULL;