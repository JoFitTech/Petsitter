CREATE TABLE IF NOT EXISTS postal_code_location (
    country_code VARCHAR(2) NOT NULL,
    postal_code VARCHAR(20) NOT NULL,
    primary_place_name VARCHAR(180) NOT NULL,
    place_names VARCHAR(1000) NOT NULL,
    latitude DECIMAL(10, 6) NOT NULL,
    longitude DECIMAL(11, 6) NOT NULL,
    fetched_at DATETIME(6) NOT NULL,
    PRIMARY KEY (country_code, postal_code)
);
