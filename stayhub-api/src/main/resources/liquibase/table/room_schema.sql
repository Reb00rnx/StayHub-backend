CREATE TABLE rooms
(
    id              UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    property_id     UUID                  REFERENCES properties (id) ON DELETE CASCADE,
    room_number     VARCHAR(50),
    type            VARCHAR(50)           NOT NULL,
    price_per_night DECIMAL(10, 2),
    max_guests      INT,
    status          VARCHAR(50)           NOT NULL,
    created_at      TIMESTAMP             DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP             DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_rooms_property_id ON rooms (property_id);
CREATE INDEX idx_rooms_status      ON rooms (status);
