CREATE TABLE booking_status_history
(
    id         UUID PRIMARY KEY  DEFAULT gen_random_uuid(),
    booking_id UUID              REFERENCES bookings (id) ON DELETE CASCADE,
    status     VARCHAR(50)       NOT NULL,
    changed_at TIMESTAMP         DEFAULT CURRENT_TIMESTAMP,
    reason     VARCHAR(500)
);

CREATE INDEX idx_bsh_booking_id ON booking_status_history (booking_id);
