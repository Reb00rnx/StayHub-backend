CREATE TABLE properties
(
    id          UUID PRIMARY KEY  DEFAULT gen_random_uuid(),
    name        VARCHAR(255)      NOT NULL,
    address     VARCHAR(255)      NOT NULL,
    city        VARCHAR(100)      NOT NULL,
    country     VARCHAR(100)      NOT NULL,
    description TEXT,
    owner_id    UUID              NOT NULL REFERENCES users (id),
    created_at  TIMESTAMP         DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP         DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_properties_owner_id ON properties (owner_id);
CREATE INDEX idx_properties_city     ON properties (city);
