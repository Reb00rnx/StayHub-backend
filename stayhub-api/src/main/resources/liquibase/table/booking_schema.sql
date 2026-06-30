CREATE TABLE bookings
(
    id          UUID PRIMARY KEY  DEFAULT gen_random_uuid(),
    room_id     UUID              REFERENCES rooms (id),
    guest_id    UUID              REFERENCES users (id),
    check_in    DATE              NOT NULL,
    check_out   DATE              NOT NULL,
    status      VARCHAR(50)       NOT NULL,
    total_price DECIMAL(10, 2),
    created_at  TIMESTAMP         DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP         DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_bookings_dates CHECK (check_out > check_in)
);

CREATE INDEX idx_bookings_room_id     ON bookings (room_id);
CREATE INDEX idx_bookings_guest_id    ON bookings (guest_id);
CREATE INDEX idx_bookings_status      ON bookings (status);
CREATE INDEX idx_bookings_check_in    ON bookings (check_in);
